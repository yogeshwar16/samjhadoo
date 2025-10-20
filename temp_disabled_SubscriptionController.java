package com.samjhadoo.controller.api;

import com.samjhadoo.dto.subscription.CreateSubscriptionRequest;
import com.samjhadoo.dto.subscription.SubscriptionResponse;
import com.samjhadoo.service.payment.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        try {
            SubscriptionResponse response = subscriptionService.createSubscription(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating subscription: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable String id) {
        try {
            SubscriptionResponse response = subscriptionService.getSubscription(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching subscription: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SubscriptionResponse>> getUserSubscriptions(
            @RequestParam String userId,
            @PageableDefault(size = 10) Pageable pageable) {
        try {
            Page<SubscriptionResponse> response = subscriptionService.getUserSubscriptions(userId, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching user subscriptions: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelSubscription(@PathVariable String id) {
        try {
            subscriptionService.cancelSubscription(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error canceling subscription: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/plans")
    public ResponseEntity<?> getAvailablePlans() {
        try {
            // Return available subscription plans
            return ResponseEntity.ok(Map.of(
                "plans", List.of(
                    Map.of(
                        "id", "basic_monthly",
                        "name", "Basic Monthly",
                        "price", 29.99,
                        "currency", "USD",
                        "billingCycle", "MONTHLY",
                        "features", List.of("Feature 1", "Feature 2")
                    ),
                    Map.of(
                        "id", "premium_yearly",
                        "name", "Premium Annual",
                        "price", 299.99,
                        "currency", "USD",
                        "billingCycle", "YEARLY",
                        "features", List.of("All Basic features", "Premium Feature 1", "Premium Feature 2")
                    )
                )
            ));
        } catch (Exception e) {
            log.error("Error fetching subscription plans: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/reactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionResponse> reactivateSubscription(@PathVariable String id) {
        try {
            // Implementation for reactivating a canceled subscription
            // This would update the subscription status and handle payment
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error reactivating subscription: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
