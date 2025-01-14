package com.example.demo.service;


import kh.gov.nbc.bakong_khqr.model.KHQRGenerateDeepLinkResponse;
import org.springframework.stereotype.Service;

@Service
public interface DeeplinkService {
    KHQRGenerateDeepLinkResponse generateDeeplink(KHQRGenerateDeepLinkResponse request);
}
