package com.example.demo.controller;

import com.example.demo.service.AiService;
import com.example.demo.service.AiServicePromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")

public class AiControllerPromptTemplate {

    @Autowired
    private AiServicePromptTemplate aiService;

    @PostMapping("/prompt1")
    public String prompt1(String statement, String language)
    {
        return aiService.promptTemplate1(statement,language);
    }

}