package com.samjhadoo.service;

import com.samjhadoo.exception.TokenRefreshException;
import com.samjhadoo.model.RefreshToken;
import com.samjhadoo.model.User;
import com.samjhadoo.repository.RefreshTokenRepository;
import com.samjhadoo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    
    @Value("${security.jwt.refresh-token-expiration}")
    private Long refreshTokenDurationMs;
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    public RefreshToken createRefreshToken(String userId, HttpServletRequest request) {
        RefreshToken refreshToken = new RefreshToken();
        
        refreshToken.setUser(userRepository.findById(Long.parseLong(userId))
            .orElseThrow(() -> new RuntimeException("User not found")));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setCreatedByIp(getClientIp(request));
        refreshToken.setRevoked(false);
        
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }
    
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new sign-in request");
        }
        return token;
    }
    
    @Transactional
    public int deleteByUserId(String userId) {
        User user = userRepository.findById(Long.parseLong(userId))
            .orElseThrow(() -> new RuntimeException("User not found"));
        return refreshTokenRepository.deleteByUser(user);
    }
    
    @Transactional
    public void revokeToken(String token, String reason, HttpServletRequest request) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshToken.setRevokedByIp(getClientIp(request));
            refreshToken.setReasonRevoked(reason);
            refreshTokenRepository.save(refreshToken);
        });
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
