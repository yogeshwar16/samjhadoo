package com.samjhadoo.service;

import com.samjhadoo.dto.request.AuthenticationRequest;
import com.samjhadoo.dto.request.RegisterRequest;
import com.samjhadoo.dto.request.TokenRefreshRequest;
import com.samjhadoo.dto.response.AuthenticationResponse;
import com.samjhadoo.exception.TokenRefreshException;
import com.samjhadoo.model.RefreshToken;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.Role;
import com.samjhadoo.model.enums.VerificationStatus;
import com.samjhadoo.repository.RefreshTokenRepository;
import com.samjhadoo.repository.UserRepository;
import com.samjhadoo.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .communityType(request.getCommunityType())
                .verificationStatus(VerificationStatus.PENDING)
                .role(Role.ROLE_USER)
                .build();
                
        user = userRepository.save(user);
        
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = createRefreshToken(user);
        
        return buildAuthResponse(user, jwtToken, refreshToken);
    }
    
    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletRequest httpRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
        
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = createRefreshToken(user);
        
        return buildAuthResponse(user, jwtToken, refreshToken);
    }
    
    @Transactional
    public AuthenticationResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        
        return refreshTokenRepository.findByToken(requestRefreshToken)
            .map(refreshToken -> {
                verifyRefreshTokenExpiration(refreshToken);
                refreshTokenRepository.delete(refreshToken);
                
                User user = refreshToken.getUser();
                String newToken = jwtService.generateToken(user);
                RefreshToken newRefreshToken = createRefreshToken(user);
                
                return buildAuthResponse(user, newToken, newRefreshToken);
            })
            .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found"));
    }
    
    @Transactional
    public void logout(TokenRefreshRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
            .ifPresent(refreshToken -> {
                refreshTokenRepository.delete(refreshToken);
                SecurityContextHolder.clearContext();
            });
    }
    
    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtService.getJwtRefreshExpirationInMs()));
        refreshToken.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(refreshToken);
    }
    
    private void verifyRefreshTokenExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new sign-in request");
        }
    }

    /**
     * Builds the authentication response with user details and tokens
     */
    private AuthenticationResponse buildAuthResponse(User user, String accessToken, RefreshToken refreshToken) {
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpirationInSeconds())
                .refreshExpiresIn(jwtService.getJwtRefreshExpirationInMs() / 1000) // Convert to seconds
                .issuedAt(Instant.now())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(new String[]{user.getRole().name()})
                .build();
    }
}
