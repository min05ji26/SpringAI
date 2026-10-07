# Policy: 맞춤 정책 추천 (과제)

**나이 · 성별 · 월 소득**을 입력하면 Groq LLM이 받을 수 있는 **대한민국 정책**을 하나 추천해주는 API + 웹 UI.

수업에서 배운 범위(ChatClient로 **보내고 → 받아오기**) 안에서 만들었다. 4주차 **퓨샷 프롬프트**를 그대로 응용했다.

> 이건 "에이전트"가 아니라 **API**다. 에이전트는 LLM이 스스로 도구를 고르고 여러 번 호출하는 구조이고,
> 여기서는 입력 → 프롬프트 → **LLM 한 번 호출** → 결과 반환이다.

## 전체 그림

```
[index.html]                     [AiController]           [AiService]                 [Groq]
나이 / 성별 / 월 소득 입력
   │  "24살, 여자, 월 150만원"
   └─ POST /policy/reco ───────▶ policyCheck(info) ───▶ 퓨샷 프롬프트 + info ───▶ gpt-oss-120b
                                                                                        │
   카드로 표시  ◀── JSON 파싱 ◀──────── String ◀──────────── .content() ◀───────────────┘
   { "policy_name": "청년월세 지원", "type": "주거" }
```

## 파일 구성

| 파일 | 역할 |
|---|---|
| `controller/AiController.java` | `POST /policy/reco` — `info` 문자열을 받아 서비스로 전달 |
| `service/AiService.java` | 퓨샷 프롬프트를 만들어 ChatClient로 호출, JSON 문자열 반환 |
| `resources/static/index.html` | 입력 폼 + 결과 카드 UI (`http://localhost:8080`) |
| `resources/application.properties` | Groq 연결 설정 (키는 파일에 직접 쓰지 않음) |

---

## 1. 퓨샷 프롬프트 (`AiService`)

원하는 출력 형식(JSON)을 **예시 2개**로 보여주고 실제 사용자 정보를 붙인다.

```java
String strPrompt = """
        사용자가 넣은 정보를 바탕으로 가장 추천하는 정책을 JSON 형식으로 바꿔주세요. 추가 설명은 포함하지말고,
        실제로 대한민국에 있는 정책을 바탕으로 추천해주세요.

        예시 1 : 21살, 여자, 월 100만원
        JSON 응답 :
        { "policy_name":"국민내일배움카드", "type":"배움" }

        예시 2 : 27살, 남자, 월 200만원
        JSON 응답 :
        { "policy_name":"청년월세 특별지원", "type":"주거" }

        사용자정보 : %s
        """.formatted(info);

return chatClient.prompt(prompt)
        .call()
        .content();
```

## 2. UI (`static/index.html`)

- 입력: 나이(숫자), 성별(버튼 선택), 월 소득(만원)
- 백엔드는 `info` 하나만 받으므로, 보낼 때 `"24살, 여자, 월 150만원"` 형태로 **합쳐서** form 파라미터로 전송
- 응답은 JSON **문자열**(`text/plain`)이라 `JSON.parse`로 꺼낸다. 모델이 가끔 ` ```json ` 으로 감싸서 주기 때문에 `{ ... }` 부분만 잘라서 파싱
- `type` 값에 따라 아이콘 표시 (🏠 주거, 📚 배움, 💰 저축, 🤝 복지 ...)
- AI 추천이라 종료됐거나 조건이 다른 정책일 수 있음 → 복지로 확인 안내 문구 표시

## 3. 테스트 결과

| 입력 | 추천 | 분류 |
|---|---|---|
| 24살, 여자, 월 150만원 | 청년월세 지원 | 주거 |
| 26살, 남자, 월 180만원 | 청년내일배움카드 | 배움 |
| 29살, 여자, 월 220만원 | 청년 전월세 지원사업 | 주거 |
| 33살, 남자, 월 300만원 | 청년내일채움공제 | 저축 |
| 19살, 남자, 월 0만원 | 기초생활보장(생계급여) | 복지 |
| 67살, 여자, 월 80만원 | 기초연금 | 소득 |

---

## 트러블슈팅

### 1. IntelliJ에서 패키지가 `com/example/demo`로 따로따로 보이고 클래스에 빨간 줄

**증상**: week03은 `com.example.demo`로 묶여 보이는데, 새로 만든 `Policy`는 폴더가 하나씩 따로 보이고 `@SpringBootApplication` 등에 빨간 줄.

**원인**: `Policy` 폴더를 프로젝트에 넣기만 하고 **Gradle 프로젝트로 연결(Link)** 하지 않았다.
IntelliJ가 그냥 일반 폴더로 인식 → `src/main/java`가 소스 루트가 아님 → 패키지로 안 묶이고, 의존성(Spring)도 못 불러와서 빨간 줄.

**해결**: `Policy/build.gradle` 우클릭 → **Link Gradle Project** (또는 Gradle 탭 `+` → `Policy/build.gradle`)

> 터미널에서 `./gradlew compileJava`는 처음부터 성공했다. 코드 문제가 아니라 **IDE 설정 문제**였다.

### 2. Gradle 연결했는데도 빨간 줄 — `Gradle javaHome is null`

**증상**: Link는 했는데 0.8초 만에 동기화가 끝나고 모듈이 안 생김. IntelliJ 로그에 `Gradle javaHome is null`.

**원인**: 다른 week 프로젝트는 Gradle JVM이 `17`로 지정돼 있었는데, 새로 링크한 `Policy`만 **Gradle JVM이 비어 있었다** → Gradle을 어떤 JDK로 돌릴지 몰라서 동기화 실패.

**해결**: Settings → Build, Execution, Deployment → Build Tools → **Gradle** → `Policy` 선택 → **Gradle JVM = 17** → Gradle 탭에서 🔄 Reload All.
(`build.gradle`의 `toolchain`이 `JavaLanguageVersion.of(17)`이라 17로 맞춘다)

### 3. 파일 이름이 빨간색 (에러 아님)

**증상**: 동기화 후에도 `Policy` 안 파일 이름이 빨갛게(주황) 보임.

**원인**: 코드 에러가 아니라 **Git 상태 색깔**. 새로 만든 파일이라 "Git에 아직 추가 안 됨(Unversioned)" 표시.

| 색 | 의미 |
|---|---|
| 빨강/주황 | Git에 추가 안 된 새 파일 |
| 초록 | 추가됨 (커밋 전) |
| 파랑 | 수정됨 |

**해결**: `git add Policy` → 초록색으로 바뀜. 진짜 컴파일 에러는 파일 이름 색이 아니라 **코드 밑 빨간 물결 밑줄**로 나온다.

### 4. 프롬프트: 예시를 그대로 베끼고, 없는 정책 이름을 지어냄

**증상**
- `21살, 여자, 월 100만원`(예시 1과 같은 입력) → 예시 1 답을 그대로 반환
- `35살, 남자, 월 250만원` → `"중장년주거지원정책"` 같은 **실제로 없는 정책 이름**

**원인**
- 퓨샷 예시는 모델이 따라 하는 **견본**이다. 예시에 지어낸 이름(`디딤돌주거정책`, `청년대출정책`)을 넣었더니 모델도 그럴듯한 이름을 지어냈다
- 예시 2(월 700만원 → 대출 정책)는 소득 기준이 어색해서 모델이 기준을 잡기 어려웠다
- `고객주문 : %s` — 피자 주문 코드에서 복사하면서 남은 문구

**해결**
- 예시를 **실제 정책 이름**으로 교체 (`국민내일배움카드`, `청년월세 특별지원`)
- 프롬프트에 `실제로 대한민국에 있는 정책을 바탕으로 추천해주세요.` 추가
- 예시 2를 `27살, 남자, 월 200만원`으로 현실적으로 수정
- `고객주문` → `사용자정보`

→ 이후 테스트 6건 모두 실제 존재하는 정책이 추천됨 (위 표)

### 5. `401: Invalid API Key` (IntelliJ에서 실행 시)

**증상**: 앱은 뜨는데 UI에서 "정책 찾기"를 누르면

```
com.openai.errors.UnauthorizedException: 401: Invalid API Key
    at ...OpenAiChatModel.call(OpenAiChatModel.java:199)
    at com.example.demo.service.AiService.policyCheck(AiService.java:41)
```

**원인**
- `application.properties`의 `spring.ai.openai.api-key=${OPEN_API_KEY}`는 **환경 변수**에서 값을 가져온다
- 터미널에서는 `export OPEN_API_KEY=...` 후 실행해서 잘 됐지만, **IntelliJ ▶ 실행에는 환경 변수가 없었다**
  (Mac 시스템 환경 변수에도 없었고, 실행 구성(Run Configuration)에 넣은 값도 저장/적용이 안 됨)
- 값을 못 찾으면 `${OPEN_API_KEY}` 글자 자체가 키로 Groq에 전송됨 → 401
- 키 자체는 정상이었다 (Groq `/models` 직접 호출 시 200)

**해결**: 키를 **Git에 안 올라가는 별도 파일**에 두고, 스프링이 그 파일을 같이 읽게 했다.

1. 프로젝트 루트(`SpringAI/`)에 `secret.properties` 생성 — Git 제외 처리

   ```properties
   OPEN_API_KEY=발급받은_키
   ```

2. `application.properties`에 한 줄 추가

   ```properties
   spring.config.import=optional:file:./secret.properties,optional:file:../secret.properties
   ```

   - `${OPEN_API_KEY}`를 환경 변수뿐 아니라 이 파일에서도 찾게 됨
   - 경로가 두 개인 이유: IntelliJ는 **`SpringAI/`** 기준으로 실행하고(`./`), `./gradlew bootRun`은 **`Policy/`** 기준으로 실행한다(`../`)
   - `optional:` — 파일이 없어도 에러 없이 넘어감 (환경 변수로 주는 경우)

→ 환경 변수 없이 IntelliJ 실행 조건 그대로 테스트해서 정상 응답 확인.

### (참고) API 키를 코드에 넣지 않는 이유

이 레포는 **public**이다. 4주차 실습 파일에 키가 `application.properties`에 그대로 들어 있었는데, 그대로 push하면 누구나 키를 볼 수 있다.
그래서 모든 주차에서 키는 `${OPEN_API_KEY}`로만 쓰고, 실제 값은 환경 변수나 Git 제외 파일(`secret.properties`)에 둔다.

---

## 실행

**IntelliJ**: 루트(`SpringAI/`)에 `secret.properties`를 만들어 두고 `PolicyApplication` ▶ 실행 → http://localhost:8080

**터미널**

```bash
export OPEN_API_KEY=your_key_here
./gradlew bootRun
```
