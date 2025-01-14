package com.example.demo.controller;


import com.example.demo.controller.dto.ChatRequest;
import com.example.demo.controller.dto.ChatResponse;
import com.example.demo.model.BookModel;
import com.example.demo.service.GenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class GenerativeController {

    private final GenAIService genAIService;

    @PostMapping
    public ChatResponse getChatResponse(@RequestBody ChatRequest request) {
        return new ChatResponse(genAIService.getResponse(request));
    }

    @PostMapping("/extended")
    public ChatResponse getChatResponseExtended(@RequestBody ChatRequest request) {
        return new ChatResponse(genAIService.getResponseExtended(request));
    }

    @PostMapping("/book")
    public BookModel getBookModelFromText(@RequestBody ChatRequest request) {
        return genAIService.getBookModelFromText(request.question());
    }
}
