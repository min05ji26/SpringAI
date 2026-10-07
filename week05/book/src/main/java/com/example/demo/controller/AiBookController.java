package com.example.demo.controller;

import com.example.demo.DTO.Book;
import com.example.demo.service.AiBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiBookController {
    @Autowired
    private AiBookService aiBookService;

    @PostMapping("/Book")
    public Book lowLevel(String title){
        return aiBookService.lowleLevel(title);
    }

    @PostMapping("/book2")
    public Book highLevel(String title){
        return aiBookService.highLevel(title);
    }
}
