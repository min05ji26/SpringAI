# SpringAI

Spring + LLM 학습 기록. 주차별로 폴더를 나눠서 정리한다.

## 주차별 정리

| 주차 | 주제 | 정리 |
|------|------|------|
| 1주차 | LLM 연동 + 채팅 UI 테스트 | [week01](week01/) |
| 2주차 | ChatModel vs ChatClient, call() vs stream() | [week02](week02/) |
| 3주차 | Ollama 로컬 모델 + PromptTemplate | [week03](week03/) |
| 4주차 | 퓨샷·스텝백 프롬프트 + Output Converter | [week04](week04/) |
| 5주차 | 임베딩 + pgvector 유사도 검색 (+ Output Converter 복습) | [week05](week05/) |

## 과제

| 과제 | 내용 | 정리 |
|------|------|------|
| 정책 추천 | 나이·성별·월 소득 → 맞춤 정책 추천 API + UI (퓨샷) | [Policy](Policy/) |

## 공통 실행 메모

- 각 주차 폴더는 독립된 Gradle 프로젝트다. (5주차는 `week05/book`, `week05/vectorstore` 두 개)
- IntelliJ에서는 루트에 `secret.properties`(git 제외)를 두면 키를 자동으로 읽는다 (Policy, week05 적용).
- API 키는 코드에 넣지 않고 환경변수로 주입한다. 실행 전 `OPEN_API_KEY` 설정 필요.
  ```bash
  export OPEN_API_KEY=발급받은_키
  ./gradlew bootRun
  ```
