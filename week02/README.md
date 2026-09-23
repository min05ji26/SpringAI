# Week 2: ChatModel vs ChatClient, call() vs stream()

같은 기능을 **ChatModel(저수준)** 과 **ChatClient(고수준)** 두 가지 방식으로 구현해보면서 차이를 익혔다.

## 전체 그림

```
ChatClient  ──감쌈──▶  ChatModel  ──HTTP──▶  Groq (OpenAI 호환)
    │
    └─ Prompt(messages + options) 을 넘기고 ChatResponse 를 받는다
```

---

## 1. Prompt = 메시지 + 옵션

```java
Prompt(List<Message> messages, ChatOptions options)
```

| 구성 | 내용 |
|---|---|
| messages | 대화 내용 (System / User / Assistant / ToolResponse) |
| options | 모델 설정 (model, temperature, maxTokens ...) |

### Message 4종

| 타입 | 누가 | 역할 |
|---|---|---|
| `SystemMessage` | 개발자 | 역할·규칙 설정 (페르소나) |
| `UserMessage` | 사용자 | 질문 |
| `AssistantMessage` | 모델 | 답변 (툴 호출 요청 포함) |
| `ToolResponseMessage` | 내 코드 | 툴 실행 결과 |

`MessageType` enum = `SYSTEM, USER, ASSISTANT, TOOL`

**핵심 개념**
- 모델은 **기억이 없다** → 매번 전체 메시지 목록을 다시 보내야 대화가 이어진다
- 모델은 **함수를 직접 못 부른다** → "이거 불러줘"라고 말만 하고, 실행은 내 코드가 한다

---

## 2. ChatModel 방식 — 직접 조립 (`AIService`)

```java
@Autowired private ChatModel chatModel;

SystemMessage systemMessage = SystemMessage.builder().text("...").build();
UserMessage   userMessage   = UserMessage.builder().text(question).build();
OpenAiChatOptions options    = OpenAiChatOptions.builder()
        .model("openai/gpt-oss-20b").temperature(0.3).maxTokens(500).build();

Prompt prompt = Prompt.builder()
        .messages(systemMessage, userMessage)
        .chatOptions(options)
        .build();

ChatResponse chatResponse = chatModel.call(prompt);
```

### 응답 꺼내는 체인

```
ChatResponse → getResult() → Generation → getOutput() → AssistantMessage → getText()
```

4단계를 거쳐야 `String`이 나온다. 이유는:
- 한 응답에 **답변 후보가 여러 개**(Generation 리스트)일 수 있고
- 각 후보가 `AssistantMessage`로 감싸져 있고
- 그 안에는 텍스트 말고 **툴 호출 정보도** 들어갈 수 있어서

`getResult()` = "첫 번째 후보를 꺼낸다"

---

## 3. ChatClient 방식 — fluent (`AiServiceByChatClient`)

```java
public AiServiceByChatClient(ChatClient.Builder builder){
    this.chatClient = builder.build();
}

return chatClient.prompt()
        .system("...")
        .user(question)
        .options(OpenAiChatOptions.builder().temperature(0.3).maxTokens(1000))
        .call()
        .content();     // 바로 String
```

위에서 20줄 걸리던 게 6줄. `.content()`가 `ChatResponse → ... → getText()` 체인을 대신 해준다.

### ChatClient 얻는 두 가지 방법

```java
// 1. Builder 주입 — 기본 시스템 메시지, 옵션, Advisor 붙일 때
ChatClient client = builder.defaultSystem("너는 비서야").build();

// 2. create() — 설정 없이 한 줄
ChatClient client = ChatClient.create(chatModel);
```

모델을 여러 개 쓸 땐 Builder 자동주입이 애매해져서 `create()` 쪽을 쓴다.

---

## 4. call() vs stream()

| | ChatModel | ChatClient |
|---|---|---|
| `call()` | `ChatResponse` | `.content()` → `String` |
| `stream()` | `Flux<ChatResponse>` | `.content()` → `Flux<String>` |

- `call()` = 블로킹, 완성본 한 번에
- `stream()` = 리액티브, 조각씩 (`Flux` 사용)
- 채팅 UI → `stream()` / 결과를 코드에서 처리만 → `call()`
- `Flux`는 **구독해야 실행**된다 (lazy)

### ChatModel 스트리밍은 직접 변환해야 한다

```java
Flux<ChatResponse> fluxResponse = chatModel.stream(prompt);
Flux<String> fluxString = fluxResponse.map(chatResponse -> {
    if (chatResponse.getResult() == null) return "";
    AssistantMessage am = chatResponse.getResult().getOutput();
    if (am == null) return "";
    String chunk = am.getText();
    if (chunk == null) return "";
    return chunk;
});
```

이 `.map()` + null 체크 덩어리가 곧 **ChatClient의 `.content()` 한 줄**이다.

---

## 5. null 체크는 언제 필요한가 (이번에 헷갈렸던 것)

**기준은 "스트리밍이냐"가 아니라 "직접 꺼내느냐"다.**

| | null 체크 |
|---|---|
| `ChatClient` + `.content()` | 불필요 — 라이브러리가 이미 해줌 |
| `ChatModel` + `call()` | 거의 불필요 — 완성된 답변 하나라 `getResult()`가 항상 있음 |
| `ChatModel` + `stream()` | **필요** |

스트리밍은 조각 중에 **텍스트가 비어있는 조각**이 섞여 온다:

```
조각1: (역할 정보만, 텍스트 없음)      ← null
조각2: "옛날"
조각3: "옛날에"
...
마지막: (토큰 사용량 등 메타데이터만)   ← null
```

첫 조각과 마지막 조각이 주로 빈 놈이라 체크 안 하면 `NullPointerException`이 난다.

> **정리:** 저수준(ChatModel)으로 직접 꺼낼 때만 내가 방어하고, 스트리밍이면 특히 필수.

---

## 6. 구현한 엔드포인트

| 경로 | 방식 | 페르소나 | temperature |
|---|---|---|---|
| `POST /ai/chat-model` | call | 영어로 답변 | 0.3 |
| `POST /ai/chat-stream-model` | stream | 영어로 답변 | 0.3 |
| `POST /ai/story-stream` | stream | 동화작가 | 0.3 |
| `POST /ai/recipe` | call | 요리사 | 0.8 |

`temperature` = 다양성. 번역처럼 정확해야 하는 건 낮게, 레시피처럼 창작이 필요한 건 높게.

---

## 7. 설정 (Groq 연결)

```properties
spring.ai.openai.api-key=${OPEN_API_KEY}
spring.ai.openai.base-url=https://api.groq.com/openai/v1
spring.ai.openai.chat.options.model=openai/gpt-oss-120b
```

OpenAI 스타터를 쓰면서 **base-url만 Groq으로** 바꿨다. Groq이 OpenAI 호환 API를 제공하기 때문에 가능하다.

---

## 다음에 볼 것 / 남은 과제

- **Advisor** — 메모리, RAG를 끼워 넣는 인터셉터
- **ChatResponse 메타데이터** — 토큰 사용량 등
- 모델명이 두 군데서 충돌 중: properties는 `gpt-oss-120b`, 코드는 `gpt-oss-20b` → **코드(런타임 옵션)가 이긴다**
- 스트리밍 엔드포인트에 `produces = MediaType.TEXT_EVENT_STREAM_VALUE` 추가 필요 (없으면 다 모아서 한 번에 나가 스트리밍 의미가 없어짐)
- `AiServiceByChatClient`가 컨트롤러에서 주석 처리되어 있어 실제 호출되는 곳이 없음

## 실행

```bash
export OPEN_API_KEY=your_key_here
./gradlew bootRun
```
