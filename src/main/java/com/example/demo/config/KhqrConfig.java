package com.example.demo.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class KhqrConfig {
    @Value("${bakong.token}")
    private String bakongToken;

    @Value("${bakong.callback-url:https://yourapp.com/callback}")
    private String callbackUrl;

    @Value("${bakong.app-icon-url:https://yourapp.com/icon.png}")
    private String appIconUrl;

    @Value("${bakong.app-name:Your App}")
    private String appName;
}