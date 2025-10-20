package com.samjhadoo.controller;

import com.samjhadoo.dto.request.AuthenticationRequest;
import com.samjhadoo.dto.request.RegisterRequest;
import com.samjhadoo.dto.response.AuthenticationResponse;
import com.samjhadoo.dto.request.TokenRefreshRequest;
import com.samjhadoo.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT token")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request, httpRequest));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and invalidate refresh token")
    public ResponseEntity<?> logout(@Valid @RequestBody TokenRefreshRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.ok().build();
    }

    // TODO: Add password reset endpoints
    // TODO: Add email verification endpoints
}
