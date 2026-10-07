package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiService {

    private ChatClient chatClient;

    public AiService(ChatClient.Builder chatClientbuilder){
        this.chatClient = chatClientbuilder.build();
    }

    //퓨샷
    public String chatOrder(String order) {
        ;
        String strPrompt = """
                고객 주문을 위한 JSON 형식으로 바꿔주세요. 추가 설명은 포함하지말고.
                
                예시 1 : 작은 피자하나, 치즈랑 토마토 소스, 페퍼로니 올려주세요.
                JSON 응답 :  
                { "size":"small",
                   "type":"normal",
                   "ingredients":["cheese","tomato sauce","pepperoni"]
                }
                
                예시 2 : 큰 피자 하나, 토마토 소스랑 바질, 모짜렐라 올려주세요
                JSON 응답 :  
                { "size":"large",
                   "type":"normal",
                   "ingredients":["cheese","tomato sauce","basil","mozzarella"]
                }
                
                고객주문 : %s
                """.formatted(order);

        Prompt prompt = Prompt.builder()
                .content(strPrompt).build();

        return  chatClient.prompt(prompt)
                .call()
                .content();
    }

    //스텝백
    public String stepBack(String question){
        String background = chatClient.prompt()
                .user("질문에 포함되어 있는 배경 지식만 먼저 설명해줘. 질문 : " + question)
                .call()
                .content();
        log.info("\n\n========================== 배경 : \n{}", background);
        return chatClient.prompt()
                .user("배경 : " + background + "/ 위 배경을 바탕으로 답변해줘. 질문 : " + question)
                .call()
                .content();
    }
}
