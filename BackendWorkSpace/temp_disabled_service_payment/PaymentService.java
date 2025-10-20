package com.samjhadoo.service.payment;

import com.samjhadoo.dto.payment.CreatePaymentRequest;
import com.samjhadoo.dto.payment.PaymentResponse;
import com.samjhadoo.dto.payment.ProcessPaymentRequest;
import com.samjhadoo.dto.payment.RefundPaymentRequest;
import com.samjhadoo.exception.PaymentProcessingException;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.payment.Payment;
import com.samjhadoo.model.payment.Payment.PaymentStatus;
import com.samjhadoo.repository.UserRepository;
import com.samjhadoo.repository.payment.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    
    @Value("${stripe.api.key}")
    private String stripeApiKey;
    
    @Value("${app.currency}")
    private String defaultCurrency;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }
    
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        // Convert amount to cents for Stripe
        long amountInCents = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        
        try {
            // Create a charge using Stripe
            ChargeCreateParams params = ChargeCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(request.getCurrency().toLowerCase())
                .setSource(request.getPaymentMethodId())
                .setDescription(request.getDescription())
                .setMetadata(createMetadata(request))
                .build();
                
            Charge charge = Charge.create(params);
            
            // Save payment to database
            Payment payment = Payment.builder()
                .user(user)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethodType())
                .paymentGatewayId(charge.getId())
                .status(mapStripeStatus(charge.getStatus()))
                .gatewayResponse(charge.toJson())
                .receiptUrl(charge.getReceiptUrl())
                .build();
                
            Payment savedPayment = paymentRepository.save(payment);
            
            return mapToPaymentResponse(savedPayment);
            
        } catch (StripeException e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Error processing payment: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
            
        return mapToPaymentResponse(payment);
    }
    
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUserPayments(String userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable)
            .map(this::mapToPaymentResponse);
    }
    
    @Transactional
    public PaymentResponse processRefund(RefundPaymentRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
            
        if (!payment.getStatus().equals(PaymentStatus.COMPLETED)) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }
        
        try {
            // Create refund in Stripe
            RefundCreateParams params = RefundCreateParams.builder()
                .setCharge(payment.getPaymentGatewayId())
                .setAmount(request.getAmount() != null ? 
                    request.getAmount().multiply(BigDecimal.valueOf(100)).longValue() : null)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .build();
                
            Refund refund = Refund.create(params);
            
            // Update payment with refund details
            payment.setRefunded(true);
            payment.setRefundAmount(new BigDecimal(refund.getAmount())
                .divide(BigDecimal.valueOf(100)));
            payment.setRefundReason(request.getReason());
            payment.setStatus(PaymentStatus.REFUNDED);
            
            Payment updatedPayment = paymentRepository.save(payment);
            
            return mapToPaymentResponse(updatedPayment);
            
        } catch (StripeException e) {
            log.error("Error processing refund: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Error processing refund: " + e.getMessage());
        }
    }
    
    @Transactional
    public void handleWebhookEvent(String payload, String signature) {
        // TODO: Implement webhook handling for payment events
        // This would handle payment_intent.succeeded, payment_intent.payment_failed, etc.
    }
    
    private Map<String, String> createMetadata(CreatePaymentRequest request) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("user_id", request.getUserId());
        metadata.put("service_type", request.getServiceType());
        metadata.put("session_id", request.getSessionId());
        return metadata;
    }
    
    private PaymentStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus.toLowerCase()) {
            case "succeeded" -> PaymentStatus.COMPLETED;
            case "pending" -> PaymentStatus.PROCESSING;
            case "failed" -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };
    }
    
    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .userId(payment.getUser().getId())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .status(payment.getStatus())
            .paymentMethod(payment.getPaymentMethod())
            .createdAt(payment.getCreatedAt())
            .receiptUrl(payment.getReceiptUrl())
            .isRefunded(payment.isRefunded())
            .refundAmount(payment.getRefundAmount())
            .build();
    }
}
