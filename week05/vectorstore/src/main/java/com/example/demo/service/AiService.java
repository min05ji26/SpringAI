package com.example.demo.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiService {
    @Autowired
    private VectorStore vectorStore;

    public void addDocument(){
        List<Document> documents = List.of(
                /*new Document("대통령 선거는 5년마다 있습니다.", Map.of("source","헌법","year",1987)),
                new Document("대통령 임기는 4년입니다.",           Map.of("source", "헌법", "year", 1980)),
                new Document("국회의원은 법률안을 심의·의결합니다.",  Map.of("source", "헌법", "year", 1987)),*/
                new Document("자동차를 사용하려면 등록을 해야 합니다.", Map.of("source", "자동차관리법"))
                /*new Document("대통령은 행정부의 수반입니다.",        Map.of("source", "헌법", "year", 1987)),
                new Document("국회의원은 4년마다 투표로 뽑습니다.",   Map.of("source", "헌법", "year", 1987)),
                new Document("승용차는 정규적인 점검이 필요합니다.",  Map.of("source", "자동차관리법")),
                new Document("대한민국은 민주공화국이다.", Map.of("source","헌법","year",1948)),
                new Document("대한민국의 주권은 국민에게 있다.", Map.of("source","헌법","year",1948)),
                new Document("국군은 국가의 안전보장과 국토방위의 의무를 진다.", Map.of("source","헌법","year",1987)),
                new Document("모든 국민은 법 앞에 평등하다.", Map.of("source","헌법","year",1948)),
                new Document("모든 국민은 신체의 자유를 가진다.", Map.of("source","헌법","year",1987)),
                new Document("모든 국민의 재산권은 보장된다.", Map.of("source","헌법","year",1987)),
                new Document("모든 국민은 교육을 받을 권리를 가진다.", Map.of("source","헌법","year",1987)),
                new Document("모든 국민은 인간다운 생활을 할 권리를 가진다.", Map.of("source","헌법","year",1987)),
                new Document("입법권은 국회에 속한다.", Map.of("source","헌법","year",1987)),
                new Document("국회는 국민의 선거로 선출된 의원으로 구성한다.", Map.of("source","헌법","year",1987)),
                new Document("국회의원의 임기는 4년이다.", Map.of("source","헌법","year",1987)),
                new Document("국회에서 의결된 법률안은 대통령이 공포한다.", Map.of("source","헌법","year",1987)),
                new Document("대통령은 국가의 원수이며 국가를 대표한다.", Map.of("source","헌법","year",1987)),
                new Document("행정권은 대통령을 수반으로 하는 정부에 속한다.", Map.of("source","헌법","year",1987)),
                new Document("대통령은 국민의 직접선거로 선출한다.", Map.of("source","헌법","year",1987)),
                new Document("대통령의 임기는 5년이며 중임할 수 없다.", Map.of("source","헌법","year",1987)),
                new Document("국무총리는 국회의 동의를 얻어 대통령이 임명한다.", Map.of("source","헌법","year",1987)),
                new Document("사법권은 법관으로 구성된 법원에 속한다.", Map.of("source","헌법","year",1987)),
                new Document("법관은 헌법과 법률에 의하여 독립하여 심판한다.", Map.of("source","헌법","year",1987)),
                new Document("헌법재판소는 헌법과 관련된 사항을 관장한다.", Map.of("source","헌법","year",1987))*/

        );
        vectorStore.add(documents);
    }

    public List<Document> searchDocument(String question){
        return vectorStore.similaritySearch(question);
    }

    //서치 리퀘스트
    public List<Document> searchDocument3(String question){
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(2)
                        .similarityThreshold(0.4)
                        .filterExpression("source == '헌법' && year >= 1987")
                        .build()
        );
    }
}
