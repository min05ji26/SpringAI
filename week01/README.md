# 1주차 - LLM 연동 및 스프링 기초

## 이번 주에 한 것

- Spring AI (`spring-ai-starter-model-openai`)로 LLM 연동
- Groq의 OpenAI 호환 API(`base-url`, `model` 지정)를 붙여서 사용
- `AIService`에서 `ChatClient`로 system/user 메시지 구성해 응답 받기
- `chat.html` 정적 페이지를 만들어 `/ai/chat` 으로 fetch, 실제로 채팅 UI에서 찍어보는 것까지

## 구조

```
week01/
├── src/main/java/com/example/demo/
│   ├── controller/AIController.java   # POST /ai/chat
│   ├── service/AIService.java         # ChatClient 호출
│   └── Test20260909Application.java
└── src/main/resources/
    ├── application.properties         # 모델/base-url 설정, 키는 환경변수
    └── static/chat.html               # 채팅 UI
```

## 실행

```bash
export OPEN_API_KEY=발급받은_키
./gradlew bootRun
# http://localhost:8080/chat.html
```

## 배운 것 정리

### Spring 컨트롤러

- `@PostMapping` 반환값에 HTML/문자열을 직접 응답하려면 `@ResponseBody` 또는 `@RestController` 필요
- 타임리프(뷰 리졸버)가 있어도 특정 메서드만 `@ResponseBody`를 붙여 뷰 리졸버를 건너뛸 수 있음
- **핵심 실수**: 따옴표(`"..."`) 안에 `aiService.chat(question)` 처럼 메서드를 그대로 써넣으면 실행되지 않고 글자 그대로 출력됨
  → 따옴표 밖에서 먼저 호출해 값을 꺼낸 뒤 문자열에 이어붙여야 함

### 개념 이해

- **RAG**: 답변 생성 전에 관련 문서를 검색해서 프롬프트에 붙여주는 "검색 + 증강" 과정 (오픈북 시험 비유)
- **시스템 / 유저 / 어시스턴트 메시지**: AI 채팅 API의 역할 구분.
  특히 어시스턴트 메시지는 "AI가 예전에 한 말"을 다시 넣어서 대화 맥락을 기억시키는 용도 (후처리와는 다른 개념)
