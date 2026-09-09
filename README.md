# SpringAI

Spring + LLM 학습 기록. 주차별로 폴더를 나눠서 정리한다.

## 주차별 정리

| 주차 | 주제 | 정리 |
|------|------|------|
| 1주차 | LLM 연동 + 채팅 UI 테스트 | [week01](week01/) |

## 공통 실행 메모

- 각 주차 폴더는 독립된 Gradle 프로젝트다.
- API 키는 코드에 넣지 않고 환경변수로 주입한다. 실행 전 `OPEN_API_KEY` 설정 필요.
  ```bash
  export OPEN_API_KEY=발급받은_키
  ./gradlew bootRun
  ```
