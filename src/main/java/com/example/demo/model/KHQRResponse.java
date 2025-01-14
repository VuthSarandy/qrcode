package com.example.demo.model;

import kh.gov.nbc.bakong_khqr.model.KHQRStatus;
import lombok.Data;

@Data
public class KHQRResponse<T> {

    private KHQRStatus khqrStatus;
    private T data;

    public void setKHQRStatus(com.example.demo.model.KHQRStatus status) {
    }
}

