package com.example.demo.controller;

import com.example.demo.dto.Hotel;
import com.example.demo.service.AiService;
import com.example.demo.service.AiService2;
import com.example.demo.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController2 {

    @Autowired
    private AiService2 aiService;


    @PostMapping("/listoutput1")
    public List<String> listOutput1(String city){
        return aiService.listOutput1(city);
    }

    @PostMapping("/listoutput2")
    public List<String> listOutput2(String city){
        return aiService.listOutput2(city);
    }

    @PostMapping("/beanoutput1")
    public Hotel beanOutput1(String city){
        return aiService.beanOutput1(city);
    }

    @PostMapping("/beanoutput2")
    public Hotel beanOutput2(String city){
        return aiService.beanOutput2(city);
    }

    @PostMapping("/beanListoutput2")
    public List<Hotel> beanListOutput2(String cities){
        return aiService.beanListOutput2(cities);
    }

    @PostMapping("/mapoutput2")
    public Map<String, Object> mapListOutput2(String hotel){
        return aiService.mapOutput2(hotel);
    }

    @PostMapping("/mapoutput1")
    public Map<String, Object> mapListOutput1(String hotel){
        return aiService.mapOutput1(hotel);
    }

}
