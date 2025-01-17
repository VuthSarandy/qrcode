package com.example.demo.service;

import com.example.demo.controller.dto.CheckPaymentResponse;
import com.example.demo.model.*;
import com.example.demo.util.TelegramBotUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.*;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KhqrServiceImpl implements KhqrService {

    private final PaymentRepository paymentRepository;
    private final TelegramBotUtil telegramBotUtil;

    @Value("${app.callback.url}")
    private String callbackBaseUrl;

    private String generateBankDeepLink(String bankType, String md5Hash, Double amount, String currency) {
        // Base URLs for bank apps
        final String ABA_APP_SCHEME = "aba://";
        final String ACLEDA_APP_SCHEME = "acledabank://";

        // Fallback URLs (Play Store)
        final String ABA_PLAYSTORE = "https://play.google.com/store/apps/details?id=com.aba.mobile";
        final String ACLEDA_PLAYSTORE = "https://play.google.com/store/apps/details?id=kh.com.acleda.mobile";

        String callbackUrl = String.format("%s/api/khqr/callback/%s", callbackBaseUrl, md5Hash);
        String encodedCallback = URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8);

        switch (bankType.toUpperCase()) {
            case "ABA":
                // ABA deep link format
                return String.format("intent://payment?md5=%s&amount=%s&currency=%s&callback=%s#Intent;" +
                                "scheme=aba;" +
                                "package=com.aba.mobile;" +
                                "S.browser_fallback_url=%s;" +
                                "end",
                        md5Hash,
                        amount,
                        currency,
                        encodedCallback,
                        ABA_PLAYSTORE
                );

            case "ACLEDA":
                // ACLEDA deep link format
                return String.format("intent://qr-payment?data=%s&amount=%s&currency=%s&callback=%s#Intent;" +
                                "scheme=acledabank;" +
                                "package=kh.com.acleda.mobile;" +
                                "S.browser_fallback_url=%s;" +
                                "end",
                        md5Hash,
                        amount,
                        currency,
                        encodedCallback,
                        ACLEDA_PLAYSTORE
                );

            default:
                return null;
        }
    }

    @Override
    @Transactional
    public QrResponse generateQr(PaymentRequest request) {
        try {
            MerchantInfo merchantInfo = createMerchantInfo(request);
            KHQRResponse<KHQRData> response = BakongKHQR.generateMerchant(merchantInfo);

            if (response.getKHQRStatus().getCode() == 0) {
                String qr = response.getData().getQr();
                String md5 = response.getData().getMd5();

                // Generate deep link for bank app
                String deepLink = generateBankDeepLink(
                        request.getBankType(),
                        md5,
                        request.getAmount(),
                        request.getCurrency()
                );

                // Save transaction
                PaymentTransaction transaction = new PaymentTransaction();
                transaction.setMd5Hash(md5);
                transaction.setAmount(request.getAmount());
                transaction.setCurrency(request.getCurrency());
                transaction.setMerchantName(request.getMerchantName());
                transaction.setBankAccount(request.getBankAccount());
                transaction.setBankType(request.getBankType());
                transaction.setStatus("PENDING");
                transaction.setDeepLink(deepLink);
                transaction.setQrCode(qr);
                log.info("QR Code: {}",qr);

                paymentRepository.save(transaction);

                return new QrResponse(
                        convertQrToBase64(qr),
                        md5,
                        request.getAmount(),
                        request.getMerchantName(),
                        request.getBankAccount(),
                        deepLink,
                        true,
                        "QR generated successfully"
                );

            } else {
                log.error("Error generating QR: {}", response.getKHQRStatus().getMessage());
                return new QrResponse(
                        null, null, request.getAmount(),
                        request.getMerchantName(),
                        request.getBankAccount(),
                        null, false,
                        response.getKHQRStatus().getMessage()
                );
            }
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            return new QrResponse(
                    null, null, request.getAmount(),
                    request.getMerchantName(),
                    request.getBankAccount(),
                    null, false,
                    "Error generating QR: " + e.getMessage()
            );
        }
    }

    @Override
    public Mono<String> checkPayment(String md5Hash) {
        log.info("Checking payment {}", md5Hash);
        WebClient webClient = WebClient.create();
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNDkzYTU5ZWM3Y2IwNDJjMyJ9LCJpYXQiOjE3MzUyNzU1ODIsImV4cCI6MTc0MzA1MTU4Mn0.lA3_LlWocp0cxLsZ-ecDvzKA9UujFiZKr2DkSSrlEEk";

        return webClient.post()
                .uri("https://api-bakong.nbc.gov.kh/v1/check_transaction_by_md5")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .bodyValue("{\"md5\": \"" + md5Hash + "\"}")
                .retrieve()
                .bodyToMono(CheckPaymentResponse.class)
                .flatMap(response -> {
                    System.out.println("Payment found: " + response);
                    boolean isPaid = response.responseCode() == 0;

                    if (isPaid) {
                        log.info("Payment successful");
                        log.info(response.data().toString());
                        Map<String, Object> data = new HashMap<>();
                        data.put("chat_id", "-1002329993288");
                        data.put("text", "Payment successful");
//                        data.put("text", response.data().toString() != null ? response.data().toString() : "No additional data");

                        telegramBotUtil.sendToTelegram(data);
                        return Mono.just("Success");
                    } else {
                        log.error("Payment check failed with response code: {}", response.responseCode());
                        return Mono.just("Failed");
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error checking payment", e);
                    return Mono.just("Failed");
                });
    }

    @Override
    public List<PaymentTransaction> getPaymentHistory(String bankAccount) {
        return paymentRepository.findByBankAccount(bankAccount);
    }

    @Override
    @Transactional
    public PaymentTransaction updatePaymentStatus(String md5Hash, String status) {
        try {
            PaymentTransaction transaction = paymentRepository.findByMd5Hash(md5Hash)
                    .orElse(null);

            if (transaction != null) {
                transaction.setStatus(status);
                return paymentRepository.save(transaction);
            }
            return null;
        } catch (Exception e) {
            log.error("Error updating payment status", e);
            return null;
        }
    }

    @Override
    public PaymentTransaction getPaymentByMd5(String md5Hash) {
        return paymentRepository.findByMd5Hash(md5Hash).orElse(null);
    }

//    @Override
//    public KHQRGenerateDeepLinkResponse generateDeeplink(KHQRGenerateDeepLinkResponse request) {
//        try {
//            // Extract relevant data from QR code
//            String qrData = request.getQrData();
//            String[] qrParts = qrData.split("0");
//            String paymentId = qrParts[1];
//            // ... (extract other relevant data from qrParts)
//
//            // Prepare KHQR request data
//            Map<String, Object> data = new HashMap<>();
//            data.put("paymentId", paymentId);
//            // ... (add other required data based on KHQR documentation)
//
//            // Add source information
//            SourceInfo sourceInfo = request.getSourceInfo();
//            Map<String, String> sourceInfoMap = new HashMap<>();
//            sourceInfoMap.put("appName", sourceInfo.getAppName());
//            sourceInfoMap.put("appIconUrl", sourceInfo.getAppIconUrl());
//            sourceInfoMap.put("appDeepLinkCallback", sourceInfo.getAppDeepLinkCallback());
//            data.put("sourceInfo", sourceInfoMap);
//
//            // Encode data as URL parameters
//            String encodedData = UriComponentsBuilder.fromUriString("")
//                    .queryParams(data)
//                    .build()
//                    .toUriString();
//
//            // Construct the deep link URL
//            String deeplinkUrl = "http://api.example.com/v1/generate_deeplink_by_qr" + encodedData;
//
//            // Call the KHQR deeplink generation API
//            ResponseEntity<String> response = restTemplate.getForEntity(deeplinkUrl, String.class);
//
//            if (response.getStatusCode() == HttpStatus.OK) {
//                // Parse response data and return the short link
//                // Assuming the response is in JSON format
//                ObjectMapper objectMapper = new ObjectMapper();
//                Map<String, String> responseData = objectMapper.readValue(response.getBody(), Map.class);
//                String shortLink = responseData.get("shortLink");
//                return new KHQRGenerateDeepLinkResponse(shortLink);
//            } else {
//                // Handle error
//                throw new RuntimeException("Failed to generate deeplink: " + response.getStatusCode() + " - " + response.getBody());
//            }
//
//        } catch (Exception e) {
//            // Handle exceptions (e.g., invalid QR code, API errors)
//            throw new RuntimeException("Error generating deeplink: " + e.getMessage(), e);
//        }
//    }


    @Override
    public KHQRGenerateDeepLinkResponse generateDeeplink(KHQRGenerateDeepLinkResponse request) {
        return null;
    }

    private String convertQrToBase64(String qrString) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrString, BarcodeFormat.QR_CODE, 300, 300);

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("Error converting QR to base64", e);
            return null;
        }
    }

    private MerchantInfo createMerchantInfo(PaymentRequest request) {
        MerchantInfo merchantInfo = new MerchantInfo();

        // Required fields
        merchantInfo.setMerchantId("MERCHANT123");
        merchantInfo.setAcquiringBank(request.getBankType());
        merchantInfo.setBakongAccountId(request.getBankAccount());

        // Business details
        merchantInfo.setMerchantName(request.getMerchantName());
        merchantInfo.setMerchantCity("Phnom Penh");
        merchantInfo.setAmount(request.getAmount());
        merchantInfo.setCurrency(KHQRCurrency.valueOf(request.getCurrency()));

        // Optional fields
        merchantInfo.setStoreLabel(request.getStoreLabel());
        merchantInfo.setTerminalLabel(request.getTerminalId());

        return merchantInfo;
    }
}