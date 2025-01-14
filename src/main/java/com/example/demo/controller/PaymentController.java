package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.service.KhqrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final KhqrService khqrService;

    @GetMapping("/")
    public String showPaymentForm(Model model) {
        model.addAttribute("paymentRequest", new PaymentRequest());
        return "index";
    }

    @PostMapping("/payment/generate")
    @ResponseBody
    public QrResponse generatePayment(@RequestBody PaymentRequest request) {
        return khqrService.generateQr(request);
    }

    @GetMapping("/payment/verify/{md5Hash}")
    @ResponseBody
    public PaymentStatusResponse verifyPayment(@PathVariable String md5Hash) {
        return khqrService.checkPayment(md5Hash);
    }
}