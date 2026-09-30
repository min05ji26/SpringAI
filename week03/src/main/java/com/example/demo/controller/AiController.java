package com.example.demo.controller;

import com.example.demo.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")

public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/chat")
    public String chat(String question){
        return aiService.chat(question);
    }

    @PostMapping("/ollamaChat")
    public Flux<String> ollamaChat(String question, String model){
        System.out.println("question = " + question);
        System.out.println("model = " + model);

        return aiService.ollamaChat(question,model);
    }
}