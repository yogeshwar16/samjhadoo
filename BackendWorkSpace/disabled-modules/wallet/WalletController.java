package com.samjhadoo.controller.api.wallet;

import com.samjhadoo.dto.wallet.TransactionDTO;
import com.samjhadoo.dto.wallet.WalletDTO;
import com.samjhadoo.exception.InsufficientBalanceException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.wallet.TransactionType;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.security.UserPrincipal;
import com.samjhadoo.service.wallet.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Wallet", description = "Wallet and credits management endpoints")
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    @Operation(summary = "Get wallet", description = "Retrieves the user's wallet information")
    public ResponseEntity<WalletDTO> getWallet(@Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            WalletDTO wallet = walletService.getWallet(user);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            log.error("Error getting wallet: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/balance")
    @Operation(summary = "Get balance", description = "Retrieves the user's current available balance")
    public ResponseEntity<Map<String, BigDecimal>> getBalance(
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            BigDecimal balance = walletService.getBalance(user);
            return ResponseEntity.ok(Map.of("balance", balance));
        } catch (Exception e) {
            log.error("Error getting balance: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/add-credits")
    @Operation(summary = "Add credits", description = "Adds credits to the user's wallet")
    public ResponseEntity<TransactionDTO> addCredits(
            @RequestParam @NotNull BigDecimal amount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String referenceId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            TransactionDTO transaction = walletService.addCredits(user, amount, description, referenceId);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid add credits request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error adding credits: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer credits", description = "Transfers credits to another user")
    public ResponseEntity<TransactionDTO> transferCredits(
            @RequestParam @NotNull Long recipientUserId,
            @RequestParam @NotNull BigDecimal amount,
            @RequestParam(required = false) String description,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            TransactionDTO transaction = walletService.transferCredits(user, recipientUserId, amount, description);
            return ResponseEntity.ok(transaction);
        } catch (InsufficientBalanceException e) {
            log.warn("Insufficient balance for transfer: {}", e.getMessage());
            return ResponseEntity.status(402).build(); // Payment Required
        } catch (IllegalArgumentException e) {
            log.warn("Invalid transfer request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error transferring credits: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get transaction history", description = "Retrieves the user's transaction history")
    public ResponseEntity<Page<TransactionDTO>> getTransactionHistory(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            Page<TransactionDTO> transactions = walletService.getTransactionHistory(user, pageable);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error getting transaction history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/transactions/type/{type}")
    @Operation(summary = "Get transactions by type", description = "Retrieves transactions filtered by type")
    public ResponseEntity<Page<TransactionDTO>> getTransactionsByType(
            @PathVariable TransactionType type,
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            Page<TransactionDTO> transactions = walletService.getTransactionsByType(user, type, pageable);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error getting transactions by type: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "Get transaction", description = "Retrieves a specific transaction by ID")
    public ResponseEntity<TransactionDTO> getTransaction(
            @PathVariable String transactionId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            TransactionDTO transaction = walletService.getTransaction(transactionId, user);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Error getting transaction: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get wallet statistics", description = "Retrieves wallet statistics and analytics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            Map<String, Object> stats = walletService.getWalletStatistics(user);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting wallet statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/set-monthly-limit")
    @Operation(summary = "Set monthly limit", description = "Sets a monthly spending limit")
    public ResponseEntity<Void> setMonthlyLimit(
            @RequestParam @NotNull BigDecimal limit,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            walletService.setMonthlyLimit(user, limit);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error setting monthly limit: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/check-balance")
    @Operation(summary = "Check sufficient balance", description = "Checks if user has sufficient balance")
    public ResponseEntity<Map<String, Boolean>> checkSufficientBalance(
            @RequestParam @NotNull BigDecimal amount,
            @Parameter(hidden = true) @CurrentUser UserPrincipal currentUser) {
        try {
            User user = currentUser.getUser();
            boolean sufficient = walletService.hasSufficientBalance(user, amount);
            return ResponseEntity.ok(Map.of("sufficient", sufficient));
        } catch (Exception e) {
            log.error("Error checking balance: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
