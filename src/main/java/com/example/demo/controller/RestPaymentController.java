package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.service.KhqrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/khqr")
@RequiredArgsConstructor
public class RestPaymentController {

    private final KhqrService khqrService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateQr(@RequestBody PaymentRequest request) {
        QrResponse response = khqrService.generateQr(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify/{md5Hash}")
    public void verifyPayment(@PathVariable String md5Hash) {
        khqrService.checkPayment(md5Hash);
    }

    @GetMapping("/history/{bankAccount}")
    public ResponseEntity<List<PaymentTransaction>> getPaymentHistory(@PathVariable String bankAccount) {
        return ResponseEntity.ok(khqrService.getPaymentHistory(bankAccount));
    }

    @PostMapping("/callback/{md5Hash}")
    public ResponseEntity<?> handleCallback(
            @PathVariable String md5Hash,
            @RequestBody Map<String, String> payload) {
        PaymentTransaction transaction = khqrService.updatePaymentStatus(md5Hash, payload.get("status"));
        return ResponseEntity.ok(transaction);
    }
}