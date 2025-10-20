package com.samjhadoo.service;

import com.samjhadoo.exception.InsufficientFundsException;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.User;
import com.samjhadoo.model.Wallet;
import com.samjhadoo.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceMockTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User testUser;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testWallet = new Wallet();
        testWallet.setId(1L);
        testWallet.setUser(testUser);
        testWallet.setBalance(BigDecimal.valueOf(100.00));
    }

    @Test
    void getOrCreateWallet_WhenWalletExists_ShouldReturnExistingWallet() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));

        // Act
        Wallet result = walletService.getOrCreateWallet();

        // Assert
        assertNotNull(result);
        assertEquals(testWallet, result);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void addFunds_WithValidAmount_ShouldUpdateBalance() {
        // Arrange
        BigDecimal amount = BigDecimal.TEN;
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        // Act
        Wallet result = walletService.addFunds(amount);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(110.00), result.getBalance());
        verify(walletRepository, times(1)).save(testWallet);
    }

    @Test
    void deductFunds_WithInsufficientBalance_ShouldThrowException() {
        // Arrange
        BigDecimal amount = BigDecimal.valueOf(200.00);
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> 
            walletService.deductFunds(amount)
        );
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void transferFunds_ValidTransfer_ShouldUpdateBothWallets() {
        // Arrange
        User recipient = new User();
        recipient.setId(2L);
        
        Wallet recipientWallet = new Wallet();
        recipientWallet.setId(2L);
        recipientWallet.setUser(recipient);
        recipientWallet.setBalance(BigDecimal.ZERO);
        
        BigDecimal amount = BigDecimal.TEN;
        
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(userService.getUserById(2L)).thenReturn(recipient);
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        when(walletRepository.findByUser(recipient)).thenReturn(Optional.of(recipientWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        boolean result = walletService.transferFunds(2L, amount);

        // Assert
        assertTrue(result);
        assertEquals(BigDecimal.valueOf(90.00), testWallet.getBalance());
        assertEquals(BigDecimal.TEN, recipientWallet.getBalance());
        verify(walletRepository, times(2)).save(any(Wallet.class));
    }

    @Test
    void getWalletBalance_WhenWalletExists_ShouldReturnBalance() {
        // Arrange
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act
        BigDecimal balance = walletService.getWalletBalance();

        // Assert
        assertEquals(BigDecimal.valueOf(100.00), balance);
    }

    @Test
    void getWalletBalance_WhenWalletNotExists_ShouldThrowException() {
        // Arrange
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            walletService.getWalletBalance()
        );
    }

    @Test
    void hasSufficientFunds_WithSufficientBalance_ShouldReturnTrue() {
        // Arrange
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act
        boolean result = walletService.hasSufficientFunds(BigDecimal.valueOf(50.00));

        // Assert
        assertTrue(result);
    }

    @Test
    void hasSufficientFunds_WithInsufficientBalance_ShouldReturnFalse() {
        // Arrange
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act
        boolean result = walletService.hasSufficientFunds(BigDecimal.valueOf(200.00));

        // Assert
        assertFalse(result);
    }

    @Test
    void transferFunds_WithSameSenderAndRecipient_ShouldFail() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(userService.getUserById(1L)).thenReturn(testUser);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            walletService.transferFunds(1L, BigDecimal.TEN)
        );
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void transferFunds_WithNegativeAmount_ShouldFail() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            walletService.transferFunds(2L, BigDecimal.valueOf(-10))
        );
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}
