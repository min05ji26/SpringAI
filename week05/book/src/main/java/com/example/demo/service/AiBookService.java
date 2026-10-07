package com.example.demo.service;

import com.example.demo.DTO.Book;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AiBookService {

    private ChatClient chatClient;

    public AiBookService(ChatClient.Builder chatClientBuilder){
        this.chatClient = chatClientBuilder.build();
    }

    public Book lowleLevel(String title){
        BeanOutputConverter<Book> beanOutputConverterconverter = new BeanOutputConverter(Book.class);
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template("책 {title}에 대한 정보를 알려주세요. {format}")
                .build();

        Prompt prompt = promptTemplate.create(Map.of("title",title,"format", beanOutputConverterconverter.getFormat()));

        String result = chatClient.prompt(prompt)
                .call()
                .content();
        return  beanOutputConverterconverter.convert(result);
    }

    public Book highLevel(String title){
        return chatClient.prompt().user("책 %s의 정보를 알려줘.".formatted(title))
                .call()
                .entity(Book.class);
    }
}
