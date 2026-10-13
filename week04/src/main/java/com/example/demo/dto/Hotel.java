package com.example.demo.dto;

import lombok.Data;

import java.util.List;

//이 모양으로 답을 해달라는 설계도
@Data
public class Hotel {
    String city;
    List<String> names;
}
