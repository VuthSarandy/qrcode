package com.example.demo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class TelegramBotUtil {

    private String telegramBaseUri = "https://api.telegram.org/bot7831908451:AAFVtkprsxe-fm1yoCMn4fEE5BZL4G5yU1c/sendMessage";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;


//    public Mono<Object> sendToTelegram(Map<String, Object> data) {
//        log.info("sendToTelegram");
//
//        return Mono.fromCallable(() -> objectMapper.writeValueAsString(data))
//                .flatMap(strJson -> webClient
//                        .post()
//                        .uri(telegramBaseUri)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body(BodyInserters.fromValue(strJson))
//                        .retrieve()
//                        .bodyToMono(Object.class))
//                .onErrorResume(e -> {
//                    log.error("Error sending telegram message: {}", e.getMessage());
//                    return Mono.just("Error sending message");
//                });
//    }
public Mono<Object> sendToTelegram(Map<String, Object> data) {
    log.info("sendToTelegram message data: {}", data); // Add this log to see the message data

    return Mono.fromCallable(() -> objectMapper.writeValueAsString(data))
            .flatMap(strJson -> {
                log.info("Sending to Telegram with payload: {}", strJson); // Add this log
                return webClient
                        .post()
                        .uri(telegramBaseUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(strJson))
                        .retrieve()
                        .bodyToMono(Object.class)
                        .doOnSuccess(response -> log.info("Telegram response: {}", response))
                        .doOnError(error -> log.error("Telegram error: {}", error.getMessage()));
            });
}
}