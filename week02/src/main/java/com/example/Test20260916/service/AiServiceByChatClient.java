package com.example.Test20260916.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiServiceByChatClient {
    private ChatClient chatClient;

    public AiServiceByChatClient(ChatClient.Builder chatClientBuilder){
        this.chatClient = chatClientBuilder.build();
    }

    public String generateText(String question){
        return this.chatClient.prompt()
                .system("응답에 대해 친절하고 이모티콘과 같이 대답해줘")
                .user(question)
                //스타트 옵션 쓸거면 옵션 설정 따로 안해도됨
                .options(OpenAiChatOptions.builder()
                        .temperature(0.3)
                        .maxTokens(1000))
                .call()
                .content();
    }

    public Flux<String> generateStreamText(String question){
        return this.chatClient.prompt()
                .system("응답에 대해 친절하고 이모티콘과 같이 대답해줘")
                .user(question)
                .options(OpenAiChatOptions.builder()
                        .temperature(0.3)
                        .maxTokens(1000))
                .stream()
                .content();
    }
}
