package com.example.demo.service;


import kh.gov.nbc.bakong_khqr.model.KHQRDeepLinkData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import kh.gov.nbc.bakong_khqr.model.KHQRStatus;
import kh.gov.nbc.bakong_khqr.model.SourceInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.apache.commons.codec.digest.DigestUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

@Service
@Slf4j
public class BakongKHQRService {

    @Value("${bakong.token}")
    private String bakongToken;

    private final RestTemplate restTemplate;

    public BakongKHQRService() {
        this.restTemplate = new RestTemplate();
    }

    public String createQr(String bankAccount,
                           String merchantCity,
                           String merchantName,
                           double amount,
                           String currency,
                           String storeLabel,
                           String phoneNumber,
                           String billNumber,
                           String terminalLabel) {

        StringBuilder qr = new StringBuilder();

        // Payload Format Indicator
        qr.append("00020101");

        // Point of Initiation Method (dynamic QR)
        qr.append("010212");

        // Merchant Account Information
        qr.append("2920");
        qr.append("0008KHBAKONG");
        appendField(qr, "01", bankAccount);

        // Merchant Category Code
        qr.append("52044816");

        // Transaction Currency (KHR=116, USD=840)
        qr.append("5303").append(currency.equals("KHR") ? "116" : "840");

        // Transaction Amount
        String amountStr = String.format("%.2f", amount);
        appendField(qr, "54", amountStr);

        // Country Code
        qr.append("5802KH");

        // Merchant Name
        appendField(qr, "59", merchantName);

        // Merchant City
        appendField(qr, "60", merchantCity);

        // Phone Number
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            appendField(qr, "62", phoneNumber);
        }

        // Store Label
        if (storeLabel != null && !storeLabel.isEmpty()) {
            appendField(qr, "64", storeLabel);
        }

        // Terminal Label
        if (terminalLabel != null && !terminalLabel.isEmpty()) {
            appendField(qr, "65", terminalLabel);
        }

        // Bill Number
        if (billNumber != null && !billNumber.isEmpty()) {
            appendField(qr, "66", billNumber);
        }

        // Calculate and append CRC
        String crc = generateMd5(qr.toString()).substring(0, 4).toUpperCase();
        qr.append("6304").append(crc);

        return qr.toString();
    }

    private void appendField(StringBuilder qr, String id, String value) {
        qr.append(id)
                .append(String.format("%02d", value.length()))
                .append(value);
    }

    public String generateMd5(String qrData) {
        return DigestUtils.md5Hex(qrData).toLowerCase();
    }

    public boolean checkPayment(String md5Hash) {
        try {
            String url = "https://api.bakong.nbc.gov.kh/v1/retail/payment/qr/status/" + md5Hash;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + bakongToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                return jsonResponse.getBoolean("paid");
            }

            return false;
        } catch (Exception e) {
            log.error("Error checking payment status: ", e);
            return false;
        }
    }

    public String generateQRImage(String qrData) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 400, 400);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("Error generating QR image: ", e);
            return null;
        }
    }


    public KHQRResponse<KHQRDeepLinkData> generateDeepLink(String url, String qr, SourceInfo sourceInfo) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            // Prepare request payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("qr", qr);
            payload.put("sourceInfo", sourceInfo);

            // Send POST request
            KHQRResponse<KHQRDeepLinkData> response = restTemplate.postForObject(
                    url,
                    payload,
                    KHQRResponse.class
            );
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            KHQRResponse<KHQRDeepLinkData> errorResponse = new KHQRResponse<>();
            KHQRStatus status = new KHQRStatus();
            status.setCode(-1);
            status.setMessage("Error generating deep link: " + e.getMessage());
            errorResponse.setKHQRStatus(status);
            return errorResponse;
        }
    }

}
