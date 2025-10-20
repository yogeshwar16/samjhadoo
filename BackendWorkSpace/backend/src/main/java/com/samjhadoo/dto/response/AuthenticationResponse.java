package com.samjhadoo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    @JsonProperty("refresh_expires_in")
    private Long refreshExpiresIn;
    
    @JsonProperty("issued_at")
    private Instant issuedAt;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("full_name")
    private String fullName;
    
    @JsonProperty("roles")
    private String[] roles;
    
    @JsonProperty("role")
    private String role;
}
