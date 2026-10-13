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
@RequestMapping("/ai")
public class AiController {
    @Autowired
    private AiService aiService;

    @PostMapping("/add")
    public void addDocument(){
        aiService.addDocument();
    }

    //string으로 이어 붙이는 방법
    @PostMapping("/search")
    public String searchDocument(String question){
        List<Document> documentlist = aiService.searchDocument(question);
        String text = "";
        for(Document document : documentlist){
            text += document.getText() + "유사도 : " +document.getScore() + document.getMetadata().get("year")+"\n";

        }
        return text;
    }

    //객체. HIT으로 이어 붙이는 방법
    //만약 몇 개인지 개수를 받아서 작성하는 식이면 매개변수 쪽에 topK를 받아와서 서비스로 넘길 수 있도록 하여도 됨
    //그럼서비스쪽은 쿼리문과 비슷하게.
    @PostMapping("/search2")
    public List<Hit> searchDocument2(String question){
        List<Document> documentlist = aiService.searchDocument(question);
        List<Hit> result = new ArrayList<>();
        for(Document document : documentlist){
           result.add(new Hit(document.getText(), document.getScore(), document.getMetadata().get("year")));

        }
        return result;
    }

    @PostMapping("/search3")
    public List<Hit> searchDocument3(String question){
        List<Document> documentlist = aiService.searchDocument3(question);
        List<Hit> result = new ArrayList<>();
        for(Document document : documentlist){
            result.add(new Hit(document.getText(), document.getScore(), document.getMetadata().get("year")));

        }
        return result;
    }
}
