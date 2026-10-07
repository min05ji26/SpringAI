package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class AiService {
    private ChatClient chatClient;

    public AiService(ChatClient.Builder chatClientbuilder){
        this.chatClient = chatClientbuilder.build();
    }

    public String policyCheck(String info) {

        String strPrompt = """
                사용자가 넣은 정보를 바탕으로 가장 추천하는 정책을 JSON 형식으로 바꿔주세요. 추가 설명은 포함하지말고,
                실제로 대한민국에 있는 정책을 바탕으로 추천해주세요.
                
                예시 1 : 21살, 여자, 월 100만원
                JSON 응답 :
                { "policy_name":"국민내일배움카드",
                   "type":"배움"
                }
                
                예시 2 : 27살, 남자, 월 200만원
                JSON 응답 :
                { "policy_name":"청년월세 특별지원",
                   "type":"주거"
                }
                
                사용자정보 : %s
                """.formatted(info);

        Prompt prompt = Prompt.builder()
                .content(strPrompt).build();

        return  chatClient.prompt(prompt)
                .call()
                .content();
    }
}
