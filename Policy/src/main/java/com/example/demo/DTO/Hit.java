package com.example.demo.DTO;

//검색 결과에서 화면에 보여줄 것만 골라담는 상자
public record Hit(String name, String type, String text, double score) {
}
