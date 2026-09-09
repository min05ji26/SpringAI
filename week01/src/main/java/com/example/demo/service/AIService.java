package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    ChatClient chatClient;

    public AIService(ChatClient.Builder chatClientBuilder){
        this.chatClient = chatClientBuilder.build();
    }

    public String chat (String question){
        String answer = this.chatClient.prompt()
                .system("모든 응답을 상냥하게 대답해줘")
                .user(question)
                .call()
                .content();
        return answer;
    }
}
