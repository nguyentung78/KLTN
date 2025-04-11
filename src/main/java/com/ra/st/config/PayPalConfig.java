package com.ra.st.config;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class PayPalConfig {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Bean
    public Map<String, String> paypalSdkConfig() {
        log.info("Configuring PayPal SDK with mode: {}", mode);
        Map<String, String> sdkConfig = new HashMap<>();
        sdkConfig.put("mode", mode);
        return sdkConfig;
    }

    @Bean
    public APIContext apiContext() throws PayPalRESTException {
        log.info("Creating APIContext with clientId: {}, mode: {}", clientId, mode);
        APIContext apiContext = new APIContext(clientId, clientSecret, mode);
        apiContext.setConfigurationMap(paypalSdkConfig());
        return apiContext;
    }
}