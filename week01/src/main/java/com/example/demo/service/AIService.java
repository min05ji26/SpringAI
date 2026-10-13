package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    ChatClient chatClient;

    //생성
    public AIService(ChatClient.Builder chatClientBuilder){
        this.chatClient = chatClientBuilder.build();
    }

    public String chat (String question){
        String answer = this.chatClient.prompt()
                .system("모든 응답을 상냥하게 대답해줘") //개발자가 정하는 규칙 페르소나
                .user(question) // 사용자가 보낸 질문
                .call()// 다 받을 때 까지 기다렸다가 한번에 보냄
                .content(); // 응답에서 텍스트만 꺼냄
        return answer;
    }
}
