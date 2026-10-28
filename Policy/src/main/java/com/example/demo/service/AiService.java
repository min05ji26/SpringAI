package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiService {
    private ChatClient chatClient;

    //벡터를 주입. 어노테이션 필수!!
    @Autowired
    private VectorStore vectorStore;

    public AiService(ChatClient.Builder chatClientbuilder){
        this.chatClient = chatClientbuilder.build();
    }

    //① 정책 자료를 벡터 DB에 넣기 (처음에 한 번만 호출)
    public void addDocument(){
        //new Document("본문 텍스트", Map.of("키",값)) 본문 -> 유사도 검색될 내용, 키값 -> 필터에 쓸 조건
        //maxIncome : 월 소득(만원) 상한. 제한 없으면 9999
        //gender : "전체" / "여자" / "남자"
        List<Document> documents = List.of(
                //---------- 주거 ----------
                new Document("청년월세 특별지원: 부모와 따로 사는 무주택 청년에게 월세를 월 최대 20만원 지원합니다. 집, 월세, 자취, 주거비 부담을 덜어줍니다.",
                        Map.of("name", "청년월세 특별지원", "type", "주거", "minAge", 19, "maxAge", 34, "maxIncome", 140, "gender", "전체")),
                new Document("청년 버팀목 전세자금대출: 무주택 청년 세대주에게 낮은 금리로 전세 보증금을 빌려줍니다. 전세집, 보증금 마련, 이사에 도움이 됩니다.",
                        Map.of("name", "청년 버팀목 전세자금대출", "type", "주거", "minAge", 19, "maxAge", 34, "maxIncome", 416, "gender", "전체")),

                //---------- 자산(저축) ----------
                new Document("청년도약계좌: 5년 동안 매달 최대 70만원을 저축하면 정부기여금과 비과세 혜택을 줍니다. 목돈 마련, 적금, 저축에 좋습니다.",
                        Map.of("name", "청년도약계좌", "type", "자산", "minAge", 19, "maxAge", 34, "maxIncome", 625, "gender", "전체")),
                new Document("청년내일저축계좌: 일하는 저소득 청년이 매달 10만원을 저축하면 정부가 같은 금액 이상을 더 넣어줍니다. 3년 뒤 목돈을 만들 수 있습니다.",
                        Map.of("name", "청년내일저축계좌", "type", "자산", "minAge", 19, "maxAge", 34, "maxIncome", 250, "gender", "전체")),
                new Document("청년 주택드림 청약통장: 청년에게 높은 이자를 주는 청약통장으로, 나중에 내 집 마련과 청약에 쓸 수 있습니다.",
                        Map.of("name", "청년 주택드림 청약통장", "type", "자산", "minAge", 19, "maxAge", 34, "maxIncome", 416, "gender", "전체")),

                //---------- 배움 ----------
                new Document("국민내일배움카드: 직업훈련과 자격증 공부에 쓰는 훈련비를 1인당 300~500만원 지원합니다. 학원, 자격증, 기술 배우기에 좋습니다.",
                        Map.of("name", "국민내일배움카드", "type", "배움", "minAge", 15, "maxAge", 75, "maxIncome", 9999, "gender", "전체")),
                new Document("국가장학금: 대학생의 소득 구간에 따라 등록금 부담을 줄여주는 장학금입니다. 대학교 등록금, 학비가 걱정될 때 신청합니다.",
                        Map.of("name", "국가장학금", "type", "배움", "minAge", 18, "maxAge", 39, "maxIncome", 9999, "gender", "전체")),

                //---------- 취업 ----------
                new Document("국민취업지원제도: 구직자에게 취업 상담과 훈련을 해주고, 조건이 되면 구직촉진수당을 월 50만원씩 최대 6개월 줍니다. 취업 준비, 구직, 일자리 찾기에 도움이 됩니다.",
                        Map.of("name", "국민취업지원제도", "type", "취업", "minAge", 15, "maxAge", 69, "maxIncome", 280, "gender", "전체")),
                new Document("여성새로일하기센터 직업교육: 경력이 끊긴 여성에게 직업교육과 취업 연결을 지원합니다. 출산, 육아 뒤 다시 일하고 싶을 때 좋습니다.",
                        Map.of("name", "여성새로일하기센터", "type", "취업", "minAge", 20, "maxAge", 64, "maxIncome", 9999, "gender", "여자")),

                //---------- 생활 ----------
                new Document("근로장려금: 일은 하지만 소득이 적은 가구에 국세청이 장려금을 줍니다. 생활비, 저소득 근로자에게 도움이 됩니다.",
                        Map.of("name", "근로장려금", "type", "생활", "minAge", 18, "maxAge", 99, "maxIncome", 183, "gender", "전체")),
                new Document("기초생활보장 생계급여: 소득이 매우 적은 가구에 매달 생계비를 지원합니다. 생활이 어렵고 생활비가 부족할 때 신청합니다.",
                        Map.of("name", "생계급여", "type", "생활", "minAge", 0, "maxAge", 99, "maxIncome", 76, "gender", "전체")),
                new Document("기초연금: 소득이 적은 65세 이상 어르신에게 매달 연금을 드립니다. 노후 생활비에 도움이 됩니다.",
                        Map.of("name", "기초연금", "type", "생활", "minAge", 65, "maxAge", 99, "maxIncome", 228, "gender", "전체")),
                new Document("여성청소년 생리용품 바우처: 저소득층 여성 청소년에게 생리용품 구입비를 매달 지원합니다.",
                        Map.of("name", "생리용품 바우처", "type", "생활", "minAge", 9, "maxAge", 24, "maxIncome", 120, "gender", "여자")),

                //---------- 교통 / 건강 ----------
                new Document("K-패스: 대중교통을 한 달에 15번 이상 타면 교통비의 일부를 돌려줍니다. 청년과 저소득층은 더 많이 돌려받습니다. 버스, 지하철, 출퇴근, 통학에 좋습니다.",
                        Map.of("name", "K-패스", "type", "교통", "minAge", 19, "maxAge", 99, "maxIncome", 9999, "gender", "전체")),
                new Document("청년 마음건강 지원: 우울감이나 스트레스가 있는 청년에게 전문 심리상담 비용을 지원합니다. 마음, 상담, 정신건강에 도움이 됩니다.",
                        Map.of("name", "청년 마음건강 지원", "type", "건강", "minAge", 19, "maxAge", 34, "maxIncome", 9999, "gender", "전체"))
        );
        vectorStore.add(documents);
    }

    //② 사용자 조건으로 정책 찾기. 조건(나이·소득·성별)은 필터로, 관련도는 유사도로
    public List<Document> searchPolicy(int age, String gender, int income, String want){
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(want)    //무엇을 찾을 지. 필수
                        .topK(3)        //몇 개까지 찾을지
                        .similarityThreshold(0.3)   //얼마나 비슷할지 (0이면 다 받음)
                        .filterExpression("minAge <= %d && maxAge >= %d && maxIncome >= %d && gender in ['전체', '%s']"
                                .formatted(age, age, income, gender))
                        .build()
        );
    }

    //③ 찾은 정책 목록을 프롬프트에 넣어서 LLM이 그 안에서만 고르게 하기
    public String policyCheck(int age, String gender, int income, String want) {
        List<Document> documentlist = searchPolicy(age, gender, income, want);

        //조건에 맞는 정책이 하나도 없으면 LLM을 부르지 않음
        if(documentlist.isEmpty()){
            return """
                    { "policy_name":"해당 없음", "type":"기타", "reason":"입력한 조건에 맞는 정책을 찾지 못했어요." }
                    """;
        }

        //검색된 정책을 string으로 이어 붙이기
        String policyList = "";
        for(Document document : documentlist){
            policyList += "- " + document.getText() + " (분류 : " + document.getMetadata().get("type") + ")\n";
        }

        String strPrompt = """
                사용자가 넣은 정보를 바탕으로 가장 추천하는 정책을 JSON 형식으로 바꿔주세요. 추가 설명은 포함하지말고,
                반드시 아래 [정책 목록] 안에서만 골라주세요. 목록에 없는 정책은 절대 만들지 마세요.
                reason에는 이 사용자에게 왜 맞는지 한 문장으로 적어주세요.

                예시 1 : 21살, 여자, 월 100만원, 원하는 것 : 자격증 공부
                JSON 응답 :
                { "policy_name":"국민내일배움카드",
                   "type":"배움",
                   "reason":"자격증 공부를 원하는 21살이라 훈련비 지원을 받을 수 있어요."
                }

                예시 2 : 27살, 남자, 월 120만원, 원하는 것 : 자취방 월세
                JSON 응답 :
                { "policy_name":"청년월세 특별지원",
                   "type":"주거",
                   "reason":"소득이 적은 무주택 청년이라 월세를 지원받을 수 있어요."
                }

                [정책 목록]
                %s
                사용자정보 : %d살, %s, 월 %d만원, 원하는 것 : %s
                """.formatted(policyList, age, gender, income, want);

        Prompt prompt = Prompt.builder()
                .content(strPrompt).build();

        return  chatClient.prompt(prompt)
                .call()
                .content();
    }
}
