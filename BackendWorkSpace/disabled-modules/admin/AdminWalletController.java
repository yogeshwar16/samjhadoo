package com.samjhadoo.controller.api.admin;

import com.samjhadoo.dto.wallet.TransactionDTO;
import com.samjhadoo.dto.wallet.WalletDTO;
import com.samjhadoo.service.wallet.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/wallets")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Wallets", description = "Admin endpoints for managing user wallets")
public class AdminWalletController {

    private final WalletService walletService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user wallet", description = "Retrieves a user's wallet information")
    public ResponseEntity<WalletDTO> getUserWallet(@PathVariable Long userId) {
        try {
            WalletDTO wallet = walletService.getWalletByUserId(userId);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            log.error("Error getting wallet for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{userId}/add-credits")
    @Operation(summary = "Add credits to user", description = "Adds credits to a user's wallet (admin)")
    public ResponseEntity<TransactionDTO> addCredits(
            @PathVariable Long userId,
            @RequestParam @NotNull BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String referenceId) {
        try {
            // This would need to get the user first
            // TransactionDTO transaction = walletService.addCredits(user, amount, description, referenceId);
            // For now, returning not implemented
            return ResponseEntity.status(501).build();
        } catch (Exception e) {
            log.error("Error adding credits: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/deduct-credits")
    @Operation(summary = "Deduct credits from user", description = "Deducts credits from a user's wallet (admin)")
    public ResponseEntity<TransactionDTO> deductCredits(
            @PathVariable Long userId,
            @RequestParam @NotNull BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String referenceId) {
        try {
            // This would need to get the user first
            // TransactionDTO transaction = walletService.deductCredits(user, amount, description, referenceId);
            return ResponseEntity.status(501).build();
        } catch (Exception e) {
            log.error("Error deducting credits: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/transactions/{transactionId}/refund")
    @Operation(summary = "Refund transaction", description = "Refunds a transaction")
    public ResponseEntity<TransactionDTO> refundTransaction(
            @PathVariable String transactionId,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String reason) {
        try {
            TransactionDTO refund = walletService.refundTransaction(transactionId, amount, reason);
            return ResponseEntity.ok(refund);
        } catch (Exception e) {
            log.error("Error refunding transaction {}: {}", transactionId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/verify")
    @Operation(summary = "Verify wallet", description = "Verifies a user's wallet (KYC)")
    public ResponseEntity<Void> verifyWallet(
            @PathVariable Long userId,
            @RequestParam int verificationLevel) {
        try {
            // This would need to get the user first
            // walletService.verifyWallet(user, verificationLevel);
            return ResponseEntity.status(501).build();
        } catch (Exception e) {
            log.error("Error verifying wallet: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/block")
    @Operation(summary = "Block wallet", description = "Blocks a user's wallet")
    public ResponseEntity<Void> blockWallet(
            @PathVariable Long userId,
            @RequestParam String reason) {
        try {
            walletService.blockWallet(userId, reason);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error blocking wallet: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/unblock")
    @Operation(summary = "Unblock wallet", description = "Unblocks a user's wallet")
    public ResponseEntity<Void> unblockWallet(@PathVariable Long userId) {
        try {
            walletService.unblockWallet(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error unblocking wallet: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{userId}/statistics")
    @Operation(summary = "Get wallet statistics", description = "Retrieves wallet statistics for a user")
    public ResponseEntity<Map<String, Object>> getWalletStatistics(@PathVariable Long userId) {
        try {
            // This would need to get the user first
            // Map<String, Object> stats = walletService.getWalletStatistics(user);
            return ResponseEntity.status(501).build();
        } catch (Exception e) {
            log.error("Error getting wallet statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
