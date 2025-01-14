package com.example.demo.service;

import com.example.demo.model.*;
import kh.gov.nbc.bakong_khqr.model.KHQRGenerateDeepLinkResponse;

import java.util.List;

public interface KhqrService {
    QrResponse generateQr(PaymentRequest request);
    PaymentStatusResponse checkPayment(String md5Hash);
    List<PaymentTransaction> getPaymentHistory(String bankAccount);
    PaymentTransaction updatePaymentStatus(String md5Hash, String status);
    PaymentTransaction getPaymentByMd5(String md5Hash);
    KHQRGenerateDeepLinkResponse generateDeeplink(KHQRGenerateDeepLinkResponse request);

}