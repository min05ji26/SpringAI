package com.example.demo.service;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiService {
    private ChatClient chatClient;

    public AiService(ChatClient.Builder chatClientBuilder){
        this.chatClient = chatClientBuilder.build();
    }

    public String chat(String question){
        return chatClient.prompt()
                .system("사용자 질문에 대해 한국어로 대답해줘")
                .options(
                        OllamaChatOptions.builder()
                                .model("exaone3.5:2.4b")
                )
                .user(question)
                .call()
                .content();
    }


    public Flux<String> ollamaChat(String question, String model){
        System.out.println("서비스 진입");
        System.out.println("question = " + question);
        System.out.println("model = " + model);

        return chatClient.prompt()
                .system("한국어로 친절하게 대답해줘")
                .options(OllamaChatOptions.builder().model(model
                ))
                .user(question)
                .stream()
                .content();
    }
}