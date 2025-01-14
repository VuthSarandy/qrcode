package com.example.demo.service;


import com.example.demo.controller.dto.ChatRequest;
import com.example.demo.model.BookModel;

public interface GenAIService {

    String getResponse(ChatRequest request);

    String getResponseExtended(ChatRequest request);

    BookModel getBookModelFromText(String question);
}
