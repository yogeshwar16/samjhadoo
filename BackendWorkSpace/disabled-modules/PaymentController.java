package com.samjhadoo.controller.api;

import com.samjhadoo.dto.payment.CreatePaymentIntentRequest;
import com.samjhadoo.dto.payment.PaymentIntentResponse;
import com.samjhadoo.dto.payment.ProcessPaymentRequest;
import com.samjhadoo.dto.payment.RefundPaymentRequest;
import com.samjhadoo.service.payment.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-payment-intent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request) {
        try {
            // In a real implementation, you would validate the request and user permissions
            PaymentIntent intent = paymentService.createPaymentIntent(
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentMethodType()
            );
            
            PaymentIntentResponse response = new PaymentIntentResponse(
                intent.getId(),
                intent.getClientSecret(),
                intent.getStatus()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (StripeException e) {
            log.error("Error creating payment intent: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                new PaymentIntentResponse(null, null, "failed")
            );
        }
    }

    @PostMapping("/process")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request) {
        try {
            // Process the payment
            var result = paymentService.processPayment(request);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> processRefund(
            @Valid @RequestBody RefundPaymentRequest request) {
        try {
            var result = paymentService.processRefund(request);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        try {
            paymentService.handleWebhookEvent(payload, signature);
            return ResponseEntity.ok(Map.of("status", "success"));
            
        } catch (Exception e) {
            log.error("Error handling webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getPaymentConfig() {
        // Return client-side configuration (e.g., Stripe publishable key)
        Map<String, Object> config = new HashMap<>();
        config.put("publishableKey", "your_stripe_publishable_key");
        config.put("currency", "usd");
        return ResponseEntity.ok(config);
    }
}
