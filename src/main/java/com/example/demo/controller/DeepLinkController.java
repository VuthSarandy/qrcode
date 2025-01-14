package com.example.demo.controller;

import com.example.demo.service.BakongKHQRService;
import kh.gov.nbc.bakong_khqr.model.KHQRDeepLinkData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import kh.gov.nbc.bakong_khqr.model.SourceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DeepLinkController {

    @Autowired
    private BakongKHQRService bakongKHQRService;

    @PostMapping("/generate-deeplink")
    public String generateDeepLink(@RequestBody SourceInfo sourceInfo) {
        String url = "http://api.example.com/v1/generate_deeplink_by_qr";
        String qr = "00020101021230440018vuth_sarandy1@aclb0111MERCHANT1230203ABA52045999530311654031005802KH5910istad shop6010Phnom Penh62220308My Store0706POS-0199170013173675490507163049B5A";

        KHQRResponse<KHQRDeepLinkData> response = bakongKHQRService.generateDeepLink(url, qr, sourceInfo);

        if (response.getKHQRStatus().getCode() == 0) {
            return response.getData().getShortLink();
        } else {
            return "Error: " + response.getKHQRStatus().getMessage();
        }
    }
}
