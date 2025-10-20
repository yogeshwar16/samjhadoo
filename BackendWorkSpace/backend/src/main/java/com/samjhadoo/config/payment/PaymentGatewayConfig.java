package com.samjhadoo.config.payment;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "payment.gateway")
public class PaymentGatewayConfig {
    private boolean enabled = true;
    private String defaultProvider = "stripe"; // or "razorpay"
    
    private StripeConfig stripe;
    private RazorpayConfig razorpay;
    
    @Data
    public static class StripeConfig {
        private boolean enabled = true;
        private String apiKey;
        private String secretKey;
        private String webhookSecret;
        private String successUrl;
        private String cancelUrl;
        private String currency = "USD";
        private boolean testMode = true;
    }
    
    @Data
    public static class RazorpayConfig {
        private boolean enabled = true;
        private String keyId;
        private String keySecret;
        private String webhookSecret;
        private String callbackUrl;
        private String currency = "INR";
        private boolean testMode = true;
    }
    
    public boolean isStripeEnabled() {
        return enabled && stripe != null && stripe.isEnabled();
    }
    
    public boolean isRazorpayEnabled() {
        return enabled && razorpay != null && razorpay.isEnabled();
    }
}
