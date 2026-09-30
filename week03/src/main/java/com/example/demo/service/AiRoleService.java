package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;

import java.util.Map;

public class AiRoleService {

    private ChatClient chatClient;
    public AiRoleService(ChatClient.Builder chatClientbuilder){
        this.chatClient = chatClientbuilder.build();

    }
    SystemPromptTemplate systemPromptTemplate = SystemPromptTemplate.builder()
            .template("""
                당신은 {role}입니다. 그 역할의 말투와 전문성으로 사용자 고민에 대해 한국어로 조언해주세요.
                """)
            .build();

    public String consult(String role, String question){
        return chatClient.prompt()
                .system(systemPromptTemplate.render(Map.of("role",role, "question", question)))
                .user(question)
                .call()
                .content();
    }
}
