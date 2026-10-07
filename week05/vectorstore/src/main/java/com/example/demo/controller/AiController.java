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

    @PostMapping("/search")
    public String searchDocument(String question){
        List<Document> documentlist = aiService.searchDocument(question);
        String text = "";
        for(Document document : documentlist){
            text += document.getText() + "유사도 : " +document.getScore() + document.getMetadata().get("year")+"\n";

        }
        return text;
    }

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
