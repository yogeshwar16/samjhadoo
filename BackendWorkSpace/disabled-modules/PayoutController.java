package com.samjhadoo.controller.api;

import com.samjhadoo.dto.payout.CreatePayoutRequest;
import com.samjhadoo.dto.payout.PayoutResponse;
import com.samjhadoo.service.payment.PayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
public class PayoutController {

    private final PayoutService payoutService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PayoutResponse> createPayout(
            @Valid @RequestBody CreatePayoutRequest request) {
        try {
            PayoutResponse response = payoutService.createPayout(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating payout: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PayoutResponse>> getPayouts(
            @RequestParam(required = false) String mentorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<PayoutResponse> response;
            if (mentorId != null) {
                response = payoutService.getPayoutsByMentor(mentorId, pageable);
            } else if (status != null) {
                response = payoutService.getPayoutsByStatus(status, pageable);
            } else if (startDate != null && endDate != null) {
                response = payoutService.getPayoutsByDateRange(startDate, endDate, pageable);
            } else {
                response = payoutService.getAllPayouts(pageable);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching payouts: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PayoutResponse> getPayout(@PathVariable String id) {
        try {
            PayoutResponse response = payoutService.getPayout(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching payout: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PayoutResponse> processPayout(@PathVariable String id) {
        try {
            PayoutResponse response = payoutService.processPayout(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing payout: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelPayout(@PathVariable String id) {
        try {
            payoutService.cancelPayout(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error canceling payout: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/mentor/{mentorId}/balance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMentorBalance(@PathVariable String mentorId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("availableBalance", payoutService.getAvailableBalance(mentorId));
            response.put("pendingPayouts", payoutService.getPendingPayoutsAmount(mentorId));
            response.put("totalEarned", payoutService.getTotalEarned(mentorId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching mentor balance: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/request")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<PayoutResponse> requestPayout(
            @RequestParam String mentorId,
            @RequestParam(required = false) Double amount) {
        try {
            PayoutResponse response = payoutService.requestPayout(mentorId, amount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error requesting payout: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
