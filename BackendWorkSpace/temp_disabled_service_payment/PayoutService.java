package com.samjhadoo.service.payment;

import com.samjhadoo.dto.payout.CreatePayoutRequest;
import com.samjhadoo.dto.payout.PayoutResponse;
import com.samjhadoo.exception.InsufficientFundsException;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.payment.Payout;
import com.samjhadoo.model.payment.Payout.PayoutStatus;
import com.samjhadoo.repository.UserRepository;
import com.samjhadoo.repository.payment.PayoutRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.param.PayoutCreateParams;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {

    private final PayoutRepository payoutRepository;
    private final UserRepository userRepository;
    
    @Value("${stripe.api.key}")
    private String stripeApiKey;
    
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;
    
    @Value("${app.payout.min-amount:50.00}")
    private BigDecimal minPayoutAmount;
    
    @Value("${app.payout.fee-percentage:2.9}")
    private double payoutFeePercentage;
    
    @Value("${app.payout.fee-fixed:0.30}")
    private double payoutFeeFixed;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }
    
    @Transactional
    public PayoutResponse createPayout(CreatePayoutRequest request) {
        User mentor = userRepository.findById(request.getMentorId())
            .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));
            
        // Calculate available balance
        BigDecimal availableBalance = getAvailableBalance(mentor.getId());
        
        // Validate payout amount
        if (request.getAmount().compareTo(availableBalance) > 0) {
            throw new InsufficientFundsException("Requested amount exceeds available balance");
        }
        
        if (request.getAmount().compareTo(minPayoutAmount) < 0) {
            throw new IllegalArgumentException("Payout amount must be at least " + minPayoutAmount);
        }
        
        // Calculate fees
        BigDecimal fee = calculatePayoutFee(request.getAmount());
        
        // Create payout record
        Payout payout = new Payout();
        payout.setMentor(mentor);
        payout.setAmount(request.getAmount());
        payout.setCurrency("USD");
        payout.setStatus(PayoutStatus.PENDING);
        payout.setPayoutMethod(request.getPayoutMethod());
        payout.setPayoutReference(UUID.randomUUID().toString());
        payout.setFeeAmount(fee);
        payout.setNetAmount(request.getAmount().subtract(fee));
        payout.setScheduledDate(LocalDateTime.now().plusDays(2)); // Process in 2 days
        
        Payout savedPayout = payoutRepository.save(payout);
        
        return mapToResponse(savedPayout);
    }
    
    @Transactional
    public PayoutResponse processPayout(String payoutId) {
        Payout payout = payoutRepository.findById(payoutId)
            .orElseThrow(() -> new ResourceNotFoundException("Payout not found"));
            
        if (payout.getStatus() != PayoutStatus.PENDING) {
            throw new IllegalStateException("Payout is not in PENDING status");
        }
        
        try {
            // Process payout through Stripe
            PayoutCreateParams params = PayoutCreateParams.builder()
                .setAmount(payout.getNetAmount().multiply(BigDecimal.valueOf(100)).longValue())
                .setCurrency(payout.getCurrency().toLowerCase())
                .setDescription("Payout for mentor: " + payout.getMentor().getId())
                .setStatementDescriptor("SAMJHADOO MENTOR")
                .build();
                
            com.stripe.model.Payout stripePayout = com.stripe.model.Payout.create(params);
            
            // Update payout status
            payout.setStatus(PayoutStatus.PROCESSING);
            payout.setTransactionId(stripePayout.getId());
            payout.setGatewayResponse(stripePayout.toJson());
            payout.setProcessedDate(LocalDateTime.now());
            
            Payout updatedPayout = payoutRepository.save(payout);
            
            // TODO: Send notification to mentor
            
            return mapToResponse(updatedPayout);
            
        } catch (StripeException e) {
            log.error("Error processing payout: {}", e.getMessage(), e);
            
            // Update payout status to failed
            payout.setStatus(PayoutStatus.FAILED);
            payout.setFailureReason(e.getMessage());
            payoutRepository.save(payout);
            
            throw new RuntimeException("Failed to process payout: " + e.getMessage(), e);
        }
    }
    
    @Scheduled(cron = "0 0 9 * * ?") // Run daily at 9 AM
    @Transactional
    public void processScheduledPayouts() {
        log.info("Processing scheduled payouts...");
        
        List<Payout> pendingPayouts = payoutRepository.findByStatusAndScheduledDateBefore(
            PayoutStatus.PENDING, 
            LocalDateTime.now()
        );
        
        log.info("Found {} payouts to process", pendingPayouts.size());
        
        for (Payout payout : pendingPayouts) {
            try {
                processPayout(payout.getId());
                log.info("Processed payout: {}", payout.getId());
            } catch (Exception e) {
                log.error("Error processing payout {}: {}", payout.getId(), e.getMessage(), e);
            }
        }
    }
    
    @Transactional(readOnly = true)
    public Page<PayoutResponse> getPayoutsByMentor(String mentorId, Pageable pageable) {
        return payoutRepository.findByMentorId(mentorId, pageable)
            .map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<PayoutResponse> getPayoutsByStatus(String status, Pageable pageable) {
        PayoutStatus payoutStatus = PayoutStatus.valueOf(status.toUpperCase());
        return payoutRepository.findByStatus(payoutStatus, pageable)
            .map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<PayoutResponse> getPayoutsByDateRange(
            LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return payoutRepository.findByProcessedDateBetween(
            startDate.atStartOfDay(),
            endDate.plusDays(1).atStartOfDay(),
            pageable
        ).map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public PayoutResponse getPayout(String id) {
        return payoutRepository.findById(id)
            .map(this::mapToResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Payout not found"));
    }
    
    @Transactional
    public void cancelPayout(String id) {
        Payout payout = payoutRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payout not found"));
            
        if (payout.getStatus() != PayoutStatus.PENDING) {
            throw new IllegalStateException("Only pending payouts can be canceled");
        }
        
        payout.setStatus(PayoutStatus.CANCELED);
        payoutRepository.save(payout);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getAvailableBalance(String mentorId) {
        // Calculate total earnings
        BigDecimal totalEarnings = payoutRepository.getTotalEarnings(mentorId);
        
        // Calculate total paid out
        BigDecimal totalPaidOut = payoutRepository.getTotalPaidOut(mentorId);
        
        // Calculate pending payouts
        BigDecimal pendingPayouts = payoutRepository.getPendingPayoutsAmount(mentorId);
        
        // Available balance = total earnings - (total paid out + pending payouts)
        return totalEarnings.subtract(totalPaidOut).subtract(pendingPayouts);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getPayoutStats(String mentorId) {
        return Map.of(
            "availableBalance", getAvailableBalance(mentorId),
            "totalEarned", payoutRepository.getTotalEarnings(mentorId),
            "totalPaidOut", payoutRepository.getTotalPaidOut(mentorId),
            "pendingPayouts", payoutRepository.getPendingPayoutsAmount(mentorId),
            "nextPayoutDate", LocalDate.now().plusDays(1) // Next business day
        );
    }
    
    private BigDecimal calculatePayoutFee(BigDecimal amount) {
        // Calculate percentage fee
        BigDecimal percentageFee = amount.multiply(BigDecimal.valueOf(payoutFeePercentage / 100));
        // Add fixed fee
        return percentageFee.add(BigDecimal.valueOf(payoutFeeFixed));
    }
    
    private PayoutResponse mapToResponse(Payout payout) {
        return PayoutResponse.builder()
            .id(payout.getId())
            .mentorId(payout.getMentor().getId())
            .mentorName(payout.getMentor().getFullName())
            .amount(payout.getAmount())
            .netAmount(payout.getNetAmount())
            .feeAmount(payout.getFeeAmount())
            .currency(payout.getCurrency())
            .status(payout.getStatus())
            .payoutMethod(payout.getPayoutMethod())
            .transactionId(payout.getTransactionId())
            .scheduledDate(payout.getScheduledDate())
            .processedDate(payout.getProcessedDate())
            .createdAt(payout.getCreatedAt())
            .build();
    }
}
