# Week 3: Ollama 로컬 모델 + PromptTemplate

로컬에서 도는 **Ollama** 모델을 붙여보고, 프롬프트를 문자열로 이어 붙이는 대신 **PromptTemplate**으로 변수만 갈아 끼우는 방식을 익혔다.

## 전체 그림

```
ChatClient ──▶ ChatModel ─┬─▶ Ollama (localhost:11434, 로컬 모델)
                          └─▶ Groq (OpenAI 호환, 클라우드)

PromptTemplate("... {language} ... {statement}")
        + Map.of("language", "영어", "statement", "안녕")
        = 완성된 프롬프트
```

---

## 1. Ollama 연결 (`AiService`)

스타터 하나만 추가하면 된다. 기본 주소는 `http://localhost:11434`.

```gradle
implementation 'org.springframework.ai:spring-ai-starter-model-ollama'
```

모델은 호출할 때 옵션으로 고른다.

```java
return chatClient.prompt()
        .system("사용자 질문에 대해 한국어로 대답해줘")
        .options(OllamaChatOptions.builder().model("exaone3.5:2.4b"))
        .user(question)
        .call()
        .content();
```

`/ai/ollamaChat`은 모델명을 **요청 파라미터로 받아서** 같은 코드로 여러 로컬 모델을 바꿔가며 테스트할 수 있게 했다 (`stream()` 사용).

> 실행 전 Ollama가 떠 있어야 하고 모델을 받아둬야 한다: `ollama pull exaone3.5:2.4b`

---

## 2. PromptTemplate — 변수 자리만 비워둔 프롬프트

```java
PromptTemplate userTemplate = PromptTemplate.builder()
        .template("다음 한국어 문장을 {language}로 번역해주세요\n문장 : {statement}")
        .build();
```

`{변수}` 자리에 `Map`으로 값을 넣는다. 시스템 메시지용은 `SystemPromptTemplate`.

| 메서드 | 반환 | 용도 |
|---|---|---|
| `create(map)` | `Prompt` | `chatClient.prompt(prompt)`에 통째로 넘김 |
| `createMessage(map)` | `Message` | `.messages(...)`에 메시지로 넘김 |
| `render(map)` | `String` | `.system()` / `.user()`에 문자열로 넘김 |

---

## 3. 같은 번역 기능, 네 가지 방식 (`AiServicePromptTemplate`)

```java
// 1. create() → Prompt 통째로
Prompt prompt = userTemplate.create(Map.of("statement", s, "language", l));
chatClient.prompt(prompt).call().content();

// 2. createMessage() → Message 목록
chatClient.prompt()
        .messages(systemTemplate.createMessage(), userTemplate.createMessage(map))
        .call().content();

// 3. render() → String
chatClient.prompt()
        .system(systemTemplate.render())
        .user(userTemplate.render(map))
        .call().content();

// 4. 템플릿 없이 텍스트 블록 + formatted()
String userText = """
        다음 한국어 문장을 %s로 번역해주세요
        문장 : %s
        """.formatted(language, statement);
```

- 1번은 system 메시지가 없다 → 번역 규칙("결과만 출력")이 안 들어가서 부연 설명이 붙을 수 있다
- 2·3번은 결과가 같다. 객체로 넘기느냐 문자열로 넘기느냐 차이
- 4번은 `%s` 순서에 의존 → 변수가 많아지면 헷갈린다. 템플릿은 **이름으로** 넣어서 순서 실수가 없다

---

## 4. 역할(페르소나)도 템플릿으로 (`AiRoleService`)

```java
SystemPromptTemplate.builder()
        .template("당신은 {role}입니다. 그 역할의 말투와 전문성으로 사용자 고민에 대해 한국어로 조언해주세요.")
        .build();
```

`role=상담사`, `role=헬스 트레이너` 처럼 **역할만 바꿔서** 같은 질문에 다른 톤의 답을 받는다.

---

## 5. 구현한 엔드포인트

| 경로 | 모델 | 내용 |
|---|---|---|
| `POST /ai/chat` | Ollama `exaone3.5:2.4b` | 한국어 답변 (call) |
| `POST /ai/ollamaChat` | Ollama (파라미터로 지정) | 한국어 답변 (stream) |
| `POST /ai/prompt1` | 기본 모델 | PromptTemplate 번역 (`statement`, `language`) |
| `POST /role` | 기본 모델 | 역할 상담 (`role`, `question`) |

---

## 6. 설정

```properties
spring.ai.openai.api-key=${OPEN_API_KEY}
spring.ai.openai.base-url=https://api.groq.com/openai/v1
spring.ai.openai.chat.options.model=openai/gpt-oss-120b
```

Ollama는 기본값(`localhost:11434`)을 써서 따로 설정이 없다.

---

## 다음에 볼 것 / 남은 과제 (현재 코드 상태)

수업 중 작성하던 상태 그대로 올려서 **지금은 빌드가 안 된다.**

- `AiControllerPromptTemplate`의 `/ai/role`이 `aiService.roleTemplate()`을 부르는데 그런 메서드가 없음 → 컴파일 에러. 역할 기능은 `AiRoleController`(`/role`)로 옮긴 것 같으니 이쪽 엔드포인트를 지우면 됨
- `AiRoleService`에 `@Service`가 없어서 `AiRoleController`에 주입이 안 됨
- `AiRoleService`에서 `render()`에 `question`도 넣고 있지만 템플릿엔 `{role}`만 있음 (질문은 `.user()`로 이미 들어감)
- Ollama·OpenAI 스타터를 **둘 다** 넣어서 `ChatModel` 빈이 2개 → `ChatClient.Builder` 자동 주입이 어떤 모델을 쓸지 애매해짐. 둘 다 쓰려면 `ChatClient.create(ollamaChatModel)` 처럼 직접 만들어야 함 (2주차 메모 참고)
- `promptTemplate2~4`는 컨트롤러에 연결 안 됨
- `/ai/ollamaChat` 스트리밍에 `produces = MediaType.TEXT_EVENT_STREAM_VALUE` 필요

## 실행

```bash
ollama serve
export OPEN_API_KEY=your_key_here
./gradlew bootRun
```
