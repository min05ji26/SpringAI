# Week 4: 프롬프트 기법(퓨샷·스텝백) + 구조화된 출력(Output Converter)

LLM 답변을 **원하는 형식**으로 받는 두 가지 방법을 익혔다.
1. 프롬프트 안에 예시를 넣어서 형식을 유도하기 (퓨샷)
2. Spring AI의 **Output Converter**로 응답을 자바 객체(`List`, `Bean`, `Map`)로 바로 변환하기

## 전체 그림

```
[프롬프트 기법]
  퓨샷   : 예시 1, 예시 2 ... + 실제 입력  ──▶ JSON 문자열
  스텝백 : 질문 ──▶ (1차 호출) 배경 지식 ──▶ (2차 호출) 배경 + 질문 ──▶ 답변

[Output Converter]
  저수준 : converter.getFormat() 을 프롬프트에 {format}으로 넣고 → content() → converter.convert()
  고수준 : .call().entity(...) 한 줄로 끝
```

---

## 1. 퓨샷(Few-shot) 프롬프트 (`AiService.chatOrder`, `ScheduleService`)

원하는 출력 예시를 2개 정도 보여주고 실제 입력을 붙인다.

```java
String strPrompt = """
        고객 주문을 위한 JSON 형식으로 바꿔주세요. 추가 설명은 포함하지말고.

        예시 1 : 작은 피자하나, 치즈랑 토마토 소스, 페퍼로니 올려주세요.
        JSON 응답 : { "size":"small", "type":"normal", "ingredients":[...] }
        ...
        고객주문 : %s
        """.formatted(order);
```

`ScheduleService`는 같은 방식으로 "내일 3시 강남역 팀 회의" → `{title, date, time, place}` JSON으로 바꾼다.
형식이 흔들리지 않도록 옵션을 고정했다.

```java
.options(OpenAiChatOptions.builder()
        .temperature(0.0)   // 매번 같은 답이 나오게
        .maxTokens(200))    // 설명이 길게 붙지 않게
```

---

## 2. 스텝백(Step-back) 프롬프트 (`AiService.stepBack`)

바로 답하지 않고 **한 발 물러서서** 배경 지식부터 정리한 뒤, 그걸 근거로 다시 질문한다. LLM을 **두 번** 호출한다.

```java
String background = chatClient.prompt()
        .user("질문에 포함되어 있는 배경 지식만 먼저 설명해줘. 질문 : " + question)
        .call().content();

return chatClient.prompt()
        .user("배경 : " + background + "/ 위 배경을 바탕으로 답변해줘. 질문 : " + question)
        .call().content();
```

---

## 3. Output Converter — 응답을 자바 객체로 (`AiService2`)

| 컨버터 | 결과 타입 | 예시 |
|---|---|---|
| `ListOutputConverter` | `List<String>` | 호텔 이름 5개 |
| `BeanOutputConverter<Hotel>` | `Hotel` (DTO) | `{ city, names[] }` |
| `BeanOutputConverter<List<Hotel>>` | `List<Hotel>` | 여러 도시의 호텔 |
| `MapOutputConverter` | `Map<String, Object>` | 호텔 상세 정보 (키가 정해져 있지 않을 때) |

### 저수준 — 직접 형식 지시문을 넣고 직접 변환

```java
ListOutputConverter converter = new ListOutputConverter();
PromptTemplate promptTemplate = PromptTemplate.builder()
        .template("{city}에서 유명한 호텔 목록 5개를 출력해줘. {format}")
        .build();

Prompt prompt = promptTemplate.create(Map.of("city", city, "format", converter.getFormat()));
String result = chatClient.prompt(prompt).call().content();
List<String> list = converter.convert(result);
```

`getFormat()`이 "쉼표로 구분된 목록으로 답해라", "이 JSON 스키마로 답해라" 같은 지시문을 만들어준다.

### 고수준 — `entity()` 한 줄

```java
chatClient.prompt()
        .user("%s에서 유명한 호텔 목록 5개를 출력해줘.".formatted(city))
        .call()
        .entity(Hotel.class);      // content() 대신 entity()
```

`entity()`가 형식 지시문 추가 + 변환을 알아서 해준다.

### 제네릭 타입(`List<Hotel>`)은 `ParameterizedTypeReference`

`List<Hotel>.class`는 문법상 불가능하므로 익명 클래스로 타입 정보를 넘긴다.

```java
.entity(new ParameterizedTypeReference<List<Hotel>>() {});
```

---

## 4. 구현한 엔드포인트

| 경로 | 파라미터 | 내용 |
|---|---|---|
| `POST /ai/order` | `order` | 퓨샷 – 피자 주문 → JSON |
| `POST /ai/schedule` | `schedule` | 퓨샷 – 일정 문장 → JSON (temperature 0) |
| `POST /ai/stepback` | `question` | 스텝백 프롬프트 |
| `POST /ai/listoutput1` / `listoutput2` | `city` | `List<String>` (저수준 / 고수준) |
| `POST /ai/beanoutput1` / `beanoutput2` | `city` | `Hotel` (저수준 / 고수준) |
| `POST /ai/beanListoutput2` | `cities` | `List<Hotel>` (고수준) |
| `POST /ai/mapoutput1` / `mapoutput2` | `hotel` | `Map<String, Object>` (저수준 / 고수준) |

---

## 5. 설정

```properties
server.port=0526
spring.ai.openai.api-key=${OPEN_API_KEY}
spring.ai.openai.base-url=https://api.groq.com/openai/v1
spring.ai.openai.chat.options.model=openai/gpt-oss-120b
```

> `0526`은 숫자 526으로 읽힌다 → `http://localhost:526`

---

## 다음에 볼 것 / 남은 과제 (현재 코드 상태)

**고친 것**
- API 키가 `application.properties`에 그대로 적혀 있었음 → `${OPEN_API_KEY}` 환경변수로 교체 (공개 레포라 키 노출 방지)
- `AiController`의 `scheduleService`에 `@Autowired`가 빠져 있어서 `/ai/schedule` 호출 시 `NullPointerException` → 추가
- `stepBack()` 로그에 `{}` 자리표시자가 없어서 배경 내용이 안 찍힘 → `{}` 추가

**남은 것**
- `beanListOutput1`(저수준 `List<Hotel>`)은 서비스에만 있고 컨트롤러에 연결 안 됨
- `new BeanOutputConverter(Hotel.class)` → 제네릭 빠져서 unchecked 경고. `new BeanOutputConverter<>(Hotel.class)`로 쓰면 깔끔
- `ScheduleService` 예시 JSON에 `"time":"15:00"` 뒤 쉼표가 빠져 있음 → 모델이 그대로 따라 하면 잘못된 JSON이 나올 수 있음
- 퓨샷 결과는 아직 `String`. Output Converter와 합치면 `Order`, `Schedule` DTO로 바로 받을 수 있다

## 실행

```bash
export OPEN_API_KEY=your_key_here
./gradlew bootRun
```
