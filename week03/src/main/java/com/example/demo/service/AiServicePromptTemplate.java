package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Flux;

import java.util.Map;
//임포트문 잘 보고 임포트 안된 것 있으면 임포트하기.
@Service
public class AiServicePromptTemplate {
    private ChatClient chatClient;
    public AiServicePromptTemplate(ChatClient.Builder chatClientbuilder){
        this.chatClient = chatClientbuilder.build();
    }

    //render은 .user, .system에 넣어주고 create는 chatClient.prompt()에 넣어줌
    //고정적으로 프롬프트를 작성
    PromptTemplate systemTemplate = SystemPromptTemplate.builder()
            .template("""
                    당신은 전문적이고 다양한 다국어 번역 AI 입니다.
                    사용자가 입력한 한국어 문장을 지정된 언어로 자연스럽게 번역하세요.
                    부연 설명이나 추가적인 인사말 없이 번역된 결과 텍스트만 간결하게 출력하세요.
                    """)
            .build();

    //가변적으로 프롬프트를 작성
    PromptTemplate userTemplate = PromptTemplate.builder()
            .template("다음 한국어 문장을 {language}로 번역해주세요\n" +
                    "문장 : {statement}")
            .build();

    //위 userTemplate을 사용하여 챗클라이언트에 넣어줌
    //create() 방식, 하나 씩만 넘길 수 있음 -> userTemplate만 넘기는 이유
    public String promptTemplate1(String statement, String language){
        Prompt prompt = userTemplate.create(Map.of("statement", statement, "language", language));
        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    //createMessage 방식, 두 개 사용 가능. 객체로 받음
    public String promptTemplate2(String statement, String language){

        return chatClient.prompt()
                .messages(systemTemplate.createMessage(),userTemplate.createMessage(Map.of("statement", statement, "language", language)))
                //시스템은 고정적이라 Map 없이 호출
                .call()
                .content();
    }

    //render 방식. String 으로 받음
    public String promptTemplate3(String statement, String language){

        return chatClient.prompt()
                .system(systemTemplate.render())
                .user(userTemplate.render(Map.of("statement", statement, "language", language)))
                .call()
                .content();
    }

    //템플릿 없이 %s 방식
    public String promptTemplate4(String statement, String language){
        String systemText = """
                당신은 전문적이고 다양한 다국어 번역 AI 입니다.
                    사용자가 입력한 한국어 문장을 지정된 언어로 자연스럽게 번역하세요.
                    부연 설명이나 추가적인 인사말 없이 번역된 결과 텍스트만 간결하게 출력하세요.
                """;
        String userText = """
                다음 한국어 문장을 %s로 번역해주세요\n
                문장 : %s
                """.formatted(language, statement);
        return chatClient.prompt()
                .system(systemText)
                .user(userText)
                .call()
                .content();
    }

}
