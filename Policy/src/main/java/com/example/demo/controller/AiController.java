package com.example.demo.controller;

import com.example.demo.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/policy")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/reco")
    public String policyCheck(String info){
        return aiService.policyCheck(info);
    }
}
