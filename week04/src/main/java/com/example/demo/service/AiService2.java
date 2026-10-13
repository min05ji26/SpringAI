package com.example.demo.service;

import com.example.demo.dto.Hotel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class AiService2 {
    private ChatClient chatClient;

    public AiService2(ChatClient.Builder chatClientbuilder){
        this.chatClient = chatClientbuilder.build();
    }

    //저수준
    public List<String> listOutput1(String city){
        ListOutputConverter converter = new ListOutputConverter();
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template("{city}에서 유명한 호텔 목록 5개를 출력해줘. {format}")
                .build();

        Prompt prompt = promptTemplate.create(
                Map.of("city", city, "format", converter.getFormat())
        );

        String result = chatClient.prompt(prompt)
                .call()
                .content();
        List<String> list = converter.convert(result);
        return list;
    }

    //고수준. (고수준 쓸 때는 content 빼기)
    public List<String> listOutput2(String city){
        return chatClient.prompt()
                .user("%s에서 유명한 호텔 목록 5개를 출력해줘.".formatted(city))
                .call()
                .entity(new ListOutputConverter());
    }

    //빈아웃풋컨버터, 컨버터는 정확이 이 모양으로 답하라는 지시문
    //리턴타입이 hotel
    public Hotel beanOutput1(String city){
        //변환해주는 문장
        BeanOutputConverter<Hotel> converter = new BeanOutputConverter(Hotel.class);
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template("{city}에서 유명한 호텔 목록 5개를 출력해줘. {format}")
                //이쪽에서 getter 제한이 걸리는데 어떻게 해야하나?
                .build();
        //builder.build()가 아닌 둘이 따로 띄어서 빌드를 마지막에 써야함

        //위에서 짠 프롬프트를 컨버터에서 가져온 포맷으로 넣기
        Prompt prompt = promptTemplate.create(
                Map.of("city", city, "format", converter.getFormat())
        );

        //챗클라이언트에 넣기
        String result = chatClient.prompt(prompt)
                .call()
                .content();
        //받은 String을 변환기로 객체로 바꾸기
        Hotel hotel= converter.convert(result);
        return hotel;
    }

    //빈아웃풋컨버터 고수준
    public Hotel beanOutput2(String city){
        return chatClient.prompt()
                .user("%s에서 유명한 호텔 목록 5개를 출력해줘.".formatted(city))
                .call()
                .entity(Hotel.class);
    }

    public List<Hotel> beanListOutput1(String cities){
        BeanOutputConverter <List<Hotel>> converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<Hotel>>() {});
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template("다음 도시들에서 유명한 호텔 3개를 출력해줘. {cities} {format}")
                .build();

        Prompt prompt = promptTemplate.create(
                Map.of("cities", cities, "format", converter.getFormat())
        );

        String result = chatClient.prompt(prompt)
                .call()
                .content();
        List<Hotel> hotelList = converter.convert((result));
        return hotelList;
    }

    public List<Hotel> beanListOutput2(String cities){
        return chatClient.prompt()
                .user("다음 도시들에서 유명한 호텔 3개를 출력해줘. %s".formatted(cities))
                .call()
                .entity(new ParameterizedTypeReference<List<Hotel>>(){});
    }

    public Map<String, Object> mapOutput2(String hotel){
        return chatClient.prompt()
                .user("호텔 %s에 대해 정보를 알려줘.".formatted(hotel))
                .call()
                .entity(new MapOutputConverter());
    }

    public Map<String, Object> mapOutput1(String hotel){
        MapOutputConverter mapOutputConverter = new MapOutputConverter();
        PromptTemplate promptTemplate = new PromptTemplate(
                "호텔 {hotel}에 대해 정보를 알려주세요 {format}");

        Prompt prompt = promptTemplate.create(
                Map.of("hotel", hotel, "format", mapOutputConverter.getFormat()));

        String json = chatClient.prompt(prompt)
                .call()
                .content();
        Map<String, Object> hotelInfo = mapOutputConverter.convert(json);
        return hotelInfo;
    }
}
