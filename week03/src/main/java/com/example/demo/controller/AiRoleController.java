package com.example.demo.controller;

import com.example.demo.service.AiRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class AiRoleController {
    @Autowired
    AiRoleService aiRoleService;

    @PostMapping("/role")
    public String role(String role, String question){
        return  aiRoleService.consult(role, question);
    }
}
