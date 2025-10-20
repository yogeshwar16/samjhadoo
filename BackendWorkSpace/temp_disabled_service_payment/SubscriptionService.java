package com.samjhadoo.service.payment;

import com.samjhadoo.dto.subscription.CreateSubscriptionRequest;
import com.samjhadoo.dto.subscription.SubscriptionResponse;
import com.samjhadoo.exception.PaymentProcessingException;
import com.samjhadoo.exception.ResourceAlreadyExistsException;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.payment.Subscription;
import com.samjhadoo.model.payment.Subscription.BillingCycle;
import com.samjhadoo.model.payment.Subscription.SubscriptionStatus;
import com.samjhadoo.repository.UserRepository;
import com.samjhadoo.repository.payment.SubscriptionRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    
    @Value("${stripe.api.key}")
    private String stripeApiKey;
    
    @Value("${stripe.prices.monthly}")
    private String monthlyPriceId;
    
    @Value("${stripe.prices.yearly}")
    private String yearlyPriceId;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }
    
    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        // Check if user already has an active subscription
        if (subscriptionRepository.hasActiveSubscription(user.getId())) {
            throw new ResourceAlreadyExistsException("User already has an active subscription");
        }
        
        try {
            // Create or retrieve Stripe customer
            String customerId = getOrCreateStripeCustomer(user, request.getPaymentMethodId());
            
            // Create subscription in Stripe
            String priceId = getPriceIdForBillingCycle(request.getBillingCycle());
            
            SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(SubscriptionCreateParams.Item.builder()
                    .setPrice(priceId)
                    .build())
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .addAllExpand(List.of("latest_invoice.payment_intent"))
                .setMetadata(createSubscriptionMetadata(user))
                .build();
                
            com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription.create(params);
            
            // Save subscription to database
            Subscription subscription = Subscription.builder()
                .user(user)
                .planId(priceId)
                .planName(getPlanName(request.getBillingCycle()))
                .amount(calculateSubscriptionAmount(request.getBillingCycle()))
                .currency("USD") // Default currency
                .billingCycle(request.getBillingCycle())
                .status(SubscriptionStatus.PENDING)
                .paymentGatewaySubscriptionId(stripeSubscription.getId())
                .paymentGatewayCustomerId(customerId)
                .startsAt(LocalDate.now())
                .nextBillingDate(calculateNextBillingDate(LocalDate.now(), request.getBillingCycle()))
                .isAutoRenew(true)
                .build();
                
            Subscription savedSubscription = subscriptionRepository.save(subscription);
            
            return mapToSubscriptionResponse(savedSubscription);
            
        } catch (StripeException e) {
            log.error("Error creating subscription: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Error creating subscription: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
            
        return mapToSubscriptionResponse(subscription);
    }
    
    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> getUserSubscriptions(String userId, Pageable pageable) {
        return subscriptionRepository.findByUserId(userId, pageable)
            .map(this::mapToSubscriptionResponse);
    }
    
    @Transactional
    public void cancelSubscription(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
            
        try {
            // Cancel subscription in Stripe
            com.stripe.model.Subscription.retrieve(subscription.getPaymentGatewaySubscriptionId())
                .cancel();
                
            // Update subscription in database
            subscriptionRepository.cancelSubscription(subscriptionId);
            
        } catch (StripeException e) {
            log.error("Error canceling subscription: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Error canceling subscription: " + e.getMessage());
        }
    }
    
    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    @Transactional
    public void processRecurringBilling() {
        LocalDate today = LocalDate.now();
        log.info("Processing recurring billing for date: {}", today);
        
        // Find subscriptions due for billing
        List<Subscription> dueSubscriptions = subscriptionRepository
            .findSubscriptionsDueForBilling(today);
            
        for (Subscription subscription : dueSubscriptions) {
            try {
                // Process payment for the subscription
                processSubscriptionPayment(subscription);
                
                // Update next billing date
                subscription.setNextBillingDate(
                    calculateNextBillingDate(subscription.getNextBillingDate(), subscription.getBillingCycle())
                );
                
                subscriptionRepository.save(subscription);
                
            } catch (Exception e) {
                log.error("Error processing subscription {}: {}", subscription.getId(), e.getMessage(), e);
                handleBillingFailure(subscription, e.getMessage());
            }
        }
    }
    
    @Scheduled(cron = "0 0 12 * * ?") // Run daily at noon
    @Transactional
    public void handleExpiringSubscriptions() {
        LocalDate today = LocalDate.now();
        log.info("Checking for expiring subscriptions on: {}", today);
        
        List<Subscription> expiringSubscriptions = subscriptionRepository
            .findExpiringSubscriptions(today.plusDays(7)); // Notify 7 days before expiration
            
        for (Subscription subscription : expiringSubscriptions) {
            // TODO: Send notification to user about upcoming subscription expiration
            log.info("Subscription {} is expiring soon for user {}", 
                subscription.getId(), subscription.getUser().getEmail());
        }
    }
    
    private void processSubscriptionPayment(Subscription subscription) {
        try {
            // In a real implementation, this would charge the customer's payment method
            // and update the subscription status accordingly
            log.info("Processing payment for subscription: {}", subscription.getId());
            
            // Simulate payment processing
            boolean paymentSuccessful = true; // In reality, this would be determined by the payment processor
            
            if (paymentSuccessful) {
                subscription.setStatus(SubscriptionStatus.ACTIVE);
            } else {
                subscription.setStatus(SubscriptionStatus.PAST_DUE);
                // TODO: Send payment failure notification
            }
            
        } catch (Exception e) {
            log.error("Error processing payment for subscription {}: {}", 
                subscription.getId(), e.getMessage(), e);
            throw new PaymentProcessingException("Error processing subscription payment");
        }
    }
    
    private void handleBillingFailure(Subscription subscription, String errorMessage) {
        // Update subscription status based on the failure
        subscription.setStatus(SubscriptionStatus.PAST_DUE);
        subscriptionRepository.save(subscription);
        
        // TODO: Send notification to user about billing failure
        log.warn("Billing failed for subscription {}: {}", subscription.getId(), errorMessage);
    }
    
    private String getOrCreateStripeCustomer(User user, String paymentMethodId) throws StripeException {
        // Check if user already has a Stripe customer ID
        Optional<Subscription> existingSubscription = subscriptionRepository
            .findByUserId(user.getId()).stream().findFirst();
            
        if (existingSubscription.isPresent() && existingSubscription.get().getPaymentGatewayCustomerId() != null) {
            return existingSubscription.get().getPaymentGatewayCustomerId();
        }
        
        // Create new Stripe customer
        CustomerCreateParams params = CustomerCreateParams.builder()
            .setEmail(user.getEmail())
            .setName(user.getFullName())
            .setPaymentMethod(paymentMethodId)
            .setInvoiceSettings(CustomerCreateParams.InvoiceSettings.builder()
                .setDefaultPaymentMethod(paymentMethodId)
                .build())
            .build();
            
        Customer customer = Customer.create(params);
        return customer.getId();
    }
    
    private String getPriceIdForBillingCycle(BillingCycle billingCycle) {
        return switch (billingCycle) {
            case MONTHLY -> monthlyPriceId;
            case YEARLY -> yearlyPriceId;
            default -> throw new IllegalArgumentException("Unsupported billing cycle: " + billingCycle);
        };
    }
    
    private String getPlanName(BillingCycle billingCycle) {
        return switch (billingCycle) {
            case MONTHLY -> "Monthly Subscription";
            case YEARLY -> "Annual Subscription";
            case QUARTERLY -> "Quarterly Subscription";
            case BIANNUAL -> "Biannual Subscription";
        };
    }
    
    private BigDecimal calculateSubscriptionAmount(BillingCycle billingCycle) {
        // In a real implementation, this would fetch from a pricing table or configuration
        return switch (billingCycle) {
            case MONTHLY -> new BigDecimal("29.99");
            case QUARTERLY -> new BigDecimal("79.99");
            case BIANNUAL -> new BigDecimal("149.99");
            case YEARLY -> new BigDecimal("299.99");
        };
    }
    
    private LocalDate calculateNextBillingDate(LocalDate currentDate, BillingCycle billingCycle) {
        return switch (billingCycle) {
            case MONTHLY -> currentDate.plusMonths(1);
            case QUARTERLY -> currentDate.plusMonths(3);
            case BIANNUAL -> currentDate.plusMonths(6);
            case YEARLY -> currentDate.plusYears(1);
        };
    }
    
    private Map<String, String> createSubscriptionMetadata(User user) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("user_id", user.getId());
        metadata.put("user_email", user.getEmail());
        return metadata;
    }
    
    private SubscriptionResponse mapToSubscriptionResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
            .id(subscription.getId())
            .userId(subscription.getUser().getId())
            .planId(subscription.getPlanId())
            .planName(subscription.getPlanName())
            .amount(subscription.getAmount())
            .currency(subscription.getCurrency())
            .billingCycle(subscription.getBillingCycle())
            .status(subscription.getStatus())
            .startsAt(subscription.getStartsAt())
            .endsAt(subscription.getEndsAt())
            .nextBillingDate(subscription.getNextBillingDate())
            .isAutoRenew(subscription.isAutoRenew())
            .createdAt(subscription.getCreatedAt())
            .build();
    }
}
