package com.example.demo.controller;

import com.example.demo.DTO.Hit;
import com.example.demo.service.AiService;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/policy")
public class AiController {

    @Autowired
    private AiService aiService;

    //정책 자료를 벡터 DB에 넣기. 두 번 부르면 같은 자료가 또 들어가니 처음 한 번만!
    @PostMapping("/add")
    public void addDocument(){
        aiService.addDocument();
    }

    //벡터 검색 결과만 보기 (LLM 없이). Hit 객체로 담아서 돌려줌
    @PostMapping("/search")
    public List<Hit> searchPolicy(int age, String gender, int income, String want){
        List<Document> documentlist = aiService.searchPolicy(age, gender, income, want);
        List<Hit> result = new ArrayList<>();
        for(Document document : documentlist){
            result.add(new Hit((String) document.getMetadata().get("name"), (String) document.getMetadata().get("type"),
                    document.getText(), document.getScore()));
        }
        return result;
    }

    //벡터 검색 + LLM 추천
    @PostMapping("/reco")
    public String policyCheck(int age, String gender, int income, String want){
        return aiService.policyCheck(age, gender, income, want);
    }
}
