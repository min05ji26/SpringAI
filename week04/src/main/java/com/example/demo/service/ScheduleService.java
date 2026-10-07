package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {

    private ChatClient chatClient;

    public ScheduleService(ChatClient.Builder chatClientbuilder){
        this.chatClient = chatClientbuilder.build();
    }

    public String schedule(String schedule) {

        String strPrompt = """
                사용자가 자유롭게 작성한 일정 문장(예 : 내일 3시 강남역 팀 회의)를 입력하면 추가 설명 없이 JSON 형태로 바꿔주세요
                
                예시 1 : 내일 3시 강남역 팀 회의
                JSON 응답 :  
                { "title":"팀 회의",
                   "date":"내일",
                   "time":"15:00"
                   "place":"강남역"
                }
                
                예시 2 : 내일 모레 인하대역 오후 8시 축제
                JSON 응답 :  
                { "title":"축제",
                   "date":"내일 모레",
                   "time":"20:00"
                   "place":"인하대역"
                }
                
                사용자일정 : %s
                """.formatted(schedule);

        Prompt prompt = Prompt.builder()
                .content(strPrompt).build();



        return  chatClient.prompt(prompt)
                .options(OpenAiChatOptions.builder()
                        .temperature(0.0)
                        .maxTokens(200))
                .call()
                .content();
    }
}
