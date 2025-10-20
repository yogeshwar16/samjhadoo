package com.samjhadoo.service;

import com.samjhadoo.model.User;
import com.samjhadoo.model.Wallet;
import com.samjhadoo.repository.WalletRepository;
import com.samjhadoo.service.impl.WalletServiceImpl;
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
class WalletServiceTest {

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
    void getOrCreateWallet_NewWallet_ShouldCreateAndReturnWallet() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        // Act
        Wallet result = walletService.getOrCreateWallet();

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void addFunds_ValidAmount_ShouldUpdateBalance() {
        // Arrange
        BigDecimal initialBalance = testWallet.getBalance();
        BigDecimal amount = BigDecimal.TEN;
        
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act
        Wallet result = walletService.addFunds(amount);

        // Assert
        assertNotNull(result);
        assertEquals(initialBalance.add(amount), result.getBalance());
        verify(walletRepository, times(1)).save(testWallet);
    }

    @Test
    void deductFunds_SufficientBalance_ShouldUpdateBalance() {
        // Arrange
        BigDecimal initialBalance = testWallet.getBalance();
        BigDecimal amount = BigDecimal.TEN;
        
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act
        boolean result = walletService.deductFunds(amount);

        // Assert
        assertTrue(result);
        assertEquals(initialBalance.subtract(amount), testWallet.getBalance());
        verify(walletRepository, times(1)).save(testWallet);
    }

    @Test
    void deductFunds_InsufficientBalance_ShouldNotUpdateBalance() {
        // Arrange
        BigDecimal amount = testWallet.getBalance().add(BigDecimal.ONE);
        
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Act
        boolean result = walletService.deductFunds(amount);

        // Assert
        assertFalse(result);
        verify(walletRepository, never()).save(any());
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
}
