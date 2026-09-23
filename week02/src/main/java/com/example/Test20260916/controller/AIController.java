package com.example.Test20260916.controller;

import com.example.Test20260916.service.AIService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;
    //private AiServiceByChatClient aiService

    @PostMapping("/chat-model")
    public String chatModel(String question){
        return aiService.generateText(question);
    }

    @PostMapping("/chat-stream-model")
    public Flux<String> chatStreamModel(String question){
        return aiService.generateStreamText(question);
    }

    @PostMapping("/story-stream")
    public Flux<String> storyteller(String topic){
        return aiService.storyteller(topic);
    }

    @PostMapping("/recipe")
    public String recipe(String ingredients){
        return aiService.recipe(ingredients);
    }
}
