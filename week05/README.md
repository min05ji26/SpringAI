# Week 5: 임베딩 + 벡터 스토어(pgvector) 유사도 검색

지금까지는 LLM에게 **질문하고 답을 받는 것**만 했다면, 이번 주는 문장을 **숫자 벡터(임베딩)** 로 바꿔 DB에 저장하고,
질문과 **의미가 가까운 문장을 찾아오는 것**(유사도 검색)을 배웠다. 다음 단계인 **RAG**의 "검색" 부분이다.

시작 전에 4주차 Output Converter를 책 정보로 한 번 더 복습했다.

| 폴더 | 내용 |
|---|---|
| [`book/`](book/) | 복습 — `BeanOutputConverter`로 책 정보를 `Book` DTO로 받기 (저수준 / 고수준) |
| [`vectorstore/`](vectorstore/) | 본 수업 — Ollama 임베딩 + pgvector 저장 + 유사도 검색 + 메타데이터 필터 |

## 전체 그림

```
[저장]  "대통령의 임기는 5년이다."  ──▶ 임베딩 모델(bge-m3, Ollama) ──▶ [0.012, -0.33, ...] ──▶ pgvector(PostgreSQL)
         + metadata {source:헌법, year:1987}                                                   (텍스트 + 벡터 + 메타데이터)

[검색]  "대통령은 몇 년마다 뽑아?"  ──▶ 임베딩 ──▶ 질문 벡터
                                                     │  저장된 벡터들과 거리 비교
                                                     ▼
                                    가까운 순으로 Document 목록 (+ score)
```

- **챗 모델**(Groq)은 대화용, **임베딩 모델**(Ollama `bge-m3`)은 문장 → 벡터 변환용. 둘은 역할이 다르다
- 의미가 비슷한 문장일수록 벡터가 가깝다 → "대통령 **임기**"와 "대통령 **몇 년마다**"는 단어가 달라도 가깝게 나온다

---

## 1. 복습: Output Converter로 책 정보 받기 (`book/`)

4주차와 같은 방식으로 `Book` DTO(`title`, `author`, `genre`, `year`)를 받는다.

```java
// 저수준 — 형식 지시문을 직접 넣고 직접 변환
BeanOutputConverter<Book> converter = new BeanOutputConverter(Book.class);
PromptTemplate promptTemplate = PromptTemplate.builder()
        .template("책 {title}에 대한 정보를 알려주세요. {format}")
        .build();
Prompt prompt = promptTemplate.create(Map.of("title", title, "format", converter.getFormat()));
String result = chatClient.prompt(prompt).call().content();
return converter.convert(result);

// 고수준 — entity() 한 줄
return chatClient.prompt().user("책 %s의 정보를 알려줘.".formatted(title))
        .call()
        .entity(Book.class);
```

실행 결과 (`title=어린 왕자`)

```
POST /ai/Book   → {"title":"어린 왕자","author":"Antoine de Saint-Exupéry","genre":"동화, 철학 소설","year":1943}
POST /ai/book2  → {"title":"The Little Prince","author":"Antoine de Saint-Exupéry","genre":"Children's literature","year":1943}
```

> 고수준은 **영어**로 답이 왔다. `entity()`가 붙이는 형식 지시문(JSON 스키마)이 영어라서 모델이 영어로 따라간 것.
> 한국어로 받으려면 프롬프트에 `"한국어로 답해줘"`를 명시해야 한다.

---

## 2. 챗 모델과 임베딩 모델을 서로 다른 곳에서 쓰기

OpenAI(Groq) 스타터와 Ollama 스타터를 **둘 다** 넣으면 모델 빈이 두 개씩 생겨서 어떤 걸 쓸지 애매해진다
(3주차 "남은 과제"에 적어둔 문제). 이번 주에 **모델 종류별로 어느 쪽을 쓸지 설정으로 지정**하는 방법을 배웠다.

```properties
spring.ai.model.chat=openai        # 채팅은 Groq(OpenAI 호환)
spring.ai.model.embedding=ollama   # 임베딩은 로컬 Ollama

spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.embedding.options.model=bge-m3
```

- Groq는 임베딩 API가 없어서 임베딩은 로컬 Ollama로 돌린다
- `bge-m3`: 다국어(한국어 포함) 임베딩 모델

---

## 3. 벡터 스토어에 저장 — `Document` + 메타데이터

```java
@Autowired
private VectorStore vectorStore;   // pgvector 스타터가 자동으로 만들어줌

List<Document> documents = List.of(
        new Document("대한민국은 민주공화국이다.",        Map.of("source", "헌법", "year", 1948)),
        new Document("대통령의 임기는 5년이며 중임할 수 없다.", Map.of("source", "헌법", "year", 1987)),
        new Document("자동차를 사용하려면 등록을 해야 합니다.", Map.of("source", "자동차관리법"))
);
vectorStore.add(documents);
```

- `Document` = **본문 텍스트 + 메타데이터(Map)**
- `add()`만 부르면 **임베딩 변환 → DB 저장**을 알아서 한다 (임베딩 모델을 직접 호출할 필요 없음)
- 메타데이터는 나중에 **검색 필터**로 쓴다 (출처, 연도 등)

---

## 4. 유사도 검색

### 기본 — `similaritySearch(String)`

```java
return vectorStore.similaritySearch(question);
```

기본값: **상위 4개**(`topK=4`), 유사도 기준 없음(다 받음). 그래서 관련 없는 문장도 섞여 나올 수 있다.

### 조건 걸기 — `SearchRequest`

```java
return vectorStore.similaritySearch(
        SearchRequest.builder()
                .query(question)
                .topK(2)                                          // 최대 2개만
                .similarityThreshold(0.4)                         // 유사도 0.4 미만은 버림
                .filterExpression("source == '헌법' && year >= 1987") // 메타데이터 필터
                .build()
);
```

| 옵션 | 의미 |
|---|---|
| `topK` | 가장 가까운 순으로 몇 개까지 가져올지 |
| `similarityThreshold` | 0~1. 이 값보다 덜 비슷한 결과는 제외 (너무 엉뚱한 결과 거르기) |
| `filterExpression` | 메타데이터 조건 (SQL `WHERE`처럼). `==`, `>=`, `&&`, `||`, `in` 등 |

→ 위 예시는 **헌법 중 1987년 이후 조항**에서만, 유사도 0.4 이상인 것 **최대 2개**를 찾는다.
`자동차관리법` 문서나 1948년 조항은 질문과 아무리 비슷해도 제외된다.

### 결과 꺼내기 — `Hit` record

```java
public record Hit(String text, double score, Object year) {}

for (Document document : documentlist) {
    result.add(new Hit(document.getText(), document.getScore(), document.getMetadata().get("year")));
}
```

- `getText()` 본문, `getScore()` 유사도(1에 가까울수록 비슷), `getMetadata()` 저장할 때 넣은 메타데이터
- `record`로 DTO를 한 줄로 만들고, 리스트로 반환하면 JSON 배열로 나간다 (`/search`의 문자열 이어 붙이기보다 깔끔)

---

## 5. 구현한 엔드포인트

**book/**

| 경로 | 파라미터 | 내용 |
|---|---|---|
| `POST /ai/Book` | `title` | 저수준 `BeanOutputConverter` → `Book` |
| `POST /ai/book2` | `title` | 고수준 `entity(Book.class)` → `Book` |

**vectorstore/**

| 경로 | 파라미터 | 내용 |
|---|---|---|
| `POST /ai/add` | - | 문서 목록을 벡터 스토어에 저장 |
| `POST /ai/search` | `question` | 기본 유사도 검색 → 문자열 |
| `POST /ai/search2` | `question` | 기본 유사도 검색 → `List<Hit>` JSON |
| `POST /ai/search3` | `question` | `SearchRequest`(topK·threshold·필터) → `List<Hit>` JSON |

---

## 6. 의존성 (vectorstore)

```gradle
implementation 'org.springframework.ai:spring-ai-starter-model-openai'          // 채팅 (Groq)
implementation 'org.springframework.ai:spring-ai-starter-model-ollama'          // 임베딩 (bge-m3)
implementation 'org.springframework.ai:spring-ai-starter-vector-store-pgvector' // PostgreSQL + pgvector
implementation 'org.springframework.ai:spring-ai-vector-store-advisor'          // 다음 단계(RAG)용 — 아직 미사용
```

---

## 다음에 볼 것 / 남은 과제 (현재 코드 상태)

**올리면서 바꾼 것**
- 두 프로젝트 모두 `application.properties`에 Groq 키가 그대로 있었음 → `${OPEN_API_KEY}`로 교체
- DB 접속 정보(URL·계정·비밀번호)도 `${DB_URL}` / `${DB_USERNAME}` / `${DB_PASSWORD}`로 교체
- 실제 값은 루트의 `secret.properties`(git 제외)에서 읽도록 `spring.config.import` 추가 → [Policy 트러블슈팅 5번](../Policy/README.md) 참고

**코드 상태**
- `addDocument()`는 헌법 문서들이 주석 처리돼 있고 **자동차관리법 1개만** 저장됨 (수업 중 테스트하던 상태)
- `/ai/add`를 여러 번 호출하면 **같은 문서가 중복 저장**된다 (id가 매번 새로 생김)
- `spring-ai-vector-store-advisor`는 추가만 돼 있음 → 다음 시간 **RAG**(`QuestionAnswerAdvisor`: 검색 결과를 프롬프트에 붙여서 LLM이 답하게)에서 쓸 예정
- `book/`: `/ai/Book`만 대문자, 메서드명 `lowleLevel` 오타, `new BeanOutputConverter(Book.class)`는 `<>` 빠져서 unchecked 경고

## 실행

```bash
# 임베딩 모델 받기 (처음 한 번)
ollama pull bge-m3
ollama serve
```

루트(`SpringAI/`)의 `secret.properties`에 아래 값이 있어야 한다.

```properties
OPEN_API_KEY=발급받은_키
DB_URL=jdbc:postgresql://<수업 DB 주소>:5432/postgres
DB_USERNAME=...
DB_PASSWORD=...
```

> 수업 DB는 **학교 내부망 주소**라 학교 네트워크에서만 접속된다. 집에서 하려면 로컬에 PostgreSQL + pgvector를 띄워야 한다.

그다음 IntelliJ에서 실행하거나 `./gradlew bootRun`.
