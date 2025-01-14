package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QrResponse {
    private String qrData;
    private String md5Hash;
    private Double amount;
    private String merchantName;
    private String bankAccount;
    private String deepLink;
    private boolean success;
    private String message;
}