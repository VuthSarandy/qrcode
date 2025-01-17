package com.example.demo.controller.dto;

import java.util.Map;
import java.util.Objects;

public record CheckPaymentResponse(

        Integer responseCode,
        String responseMessage,
        Integer errorCode,

        Map<String, Object> data

) {
}
