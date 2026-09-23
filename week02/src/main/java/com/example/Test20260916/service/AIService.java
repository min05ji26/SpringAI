package com.example.Test20260916.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AIService {

    @Autowired
    private ChatModel chatModel;

    public String generateText(String question){
        SystemMessage systemMessage = SystemMessage.builder()
                //질문의 형식이나 페르소나 같은 것
                .text("사용자 질문에 대해 영어로 답변하게 해줘")
                .build();
        UserMessage userMessage = UserMessage.builder()
                .text(question)
                .build();
        //모델의 형식이나 다양성 - 챗 옵션?
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model("openai/gpt-oss-20b")
                .temperature(0.3)   //다양성
                .maxTokens(500)
                .build();

        Prompt prompt = Prompt.builder()
                .messages(systemMessage, userMessage)
                .chatOptions(chatOptions)
                .build();

        ChatResponse chatResponse = this.chatModel.call(prompt);
        Generation generation = chatResponse.getResult();
        AssistantMessage assistantMessage = generation.getOutput();
        String answer = assistantMessage.getText();
        return answer;
    }

        public Flux<String> generateStreamText(String question) {
            SystemMessage systemMessage = SystemMessage.builder()
                    .text("사용자 질문에 대해 영어로 답변하게 해줘")
                    .build();

            UserMessage userMessage = UserMessage.builder()
                    .text(question)
                    .build();

            OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                    .model("openai/gpt-oss-20b")
                    .temperature(0.3)   //다양성
                    .maxTokens(500)
                    .build();

            Prompt prompt = Prompt.builder()
                    .messages(systemMessage, userMessage)
                    .chatOptions(chatOptions)
                    .build();

            Flux<ChatResponse> fluxResponse = this.chatModel.stream(prompt);
            Flux<String> fluxString = fluxResponse.map(chatResponse -> {
                if (chatResponse.getResult() == null) return "";

                AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
                if (assistantMessage == null) return "";
                String chunk = assistantMessage.getText();
                if (chunk == null) return "";
                return chunk;
            });
            return fluxString;
        }

        public Flux<String> storyteller(String topic){
            SystemMessage systemMessage = SystemMessage.builder()
                    .text("너는 동화를 읽어주는 작가야 아이들이 이해할법한 내용으로 1000자 이하로 친절하고 상냥하게 얘기해주면 돼")
                    .build();

            UserMessage userMessage = UserMessage.builder()
                    .text(topic)
                    .build();

            OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                    .model("openai/gpt-oss-20b")
                    .temperature(0.3)
                    .maxTokens(1000)
                    .build();

            Prompt prompt = Prompt.builder()
                    .messages(systemMessage, userMessage)
                    .chatOptions(chatOptions)
                    .build();

            Flux<ChatResponse> fluxResponse = this.chatModel.stream(prompt);
            Flux<String> fluxString = fluxResponse.map(chatResponse -> {
                if (chatResponse.getResult() == null) return "";

                AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
                if (assistantMessage == null) return "";
                String chunk = assistantMessage.getText();
                if (chunk == null) return "";
                return chunk;
            });
            return fluxString;
        }

    public String recipe(String ingredients){
        SystemMessage systemMessage = SystemMessage.builder()
                .text("너는 전문 요리사야 한국어로 요리명과 그 요리의 간단한 조리법에 대해 지시해주면 돼")
                .build();

        UserMessage userMessage = UserMessage.builder()
                .text(ingredients)
                .build();

        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model("openai/gpt-oss-20b")
                .temperature(0.8)   //다양성을 주기 위해
                .maxTokens(500)
                .build();

        Prompt prompt = Prompt.builder()
                .messages(systemMessage, userMessage)
                .chatOptions(chatOptions)
                .build();

        ChatResponse chatResponse = this.chatModel.call(prompt);
        Generation generation = chatResponse.getResult();
        AssistantMessage assistantMessage = generation.getOutput();
        String answer = assistantMessage.getText();
        return answer;
    }
}



