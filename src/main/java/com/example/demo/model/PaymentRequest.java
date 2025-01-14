package com.example.demo.model;

import lombok.Data;

@Data
public class PaymentRequest {
    private Double amount;
    private String currency;
    private String merchantName;
    private String bankAccount;
    private String storeLabel;
    private String terminalId;
    private String bankType;
}