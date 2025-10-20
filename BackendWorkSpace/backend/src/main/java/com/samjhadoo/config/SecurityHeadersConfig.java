package com.samjhadoo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
public class SecurityHeadersConfig {

    @Bean
    public SecurityFilterChain securityHeadersFilterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000) // 1 year
                )
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' cdn.jsdelivr.net; " +
                        "style-src 'self' 'unsafe-inline' cdn.jsdelivr.net; " +
                        "img-src 'self' data:; " +
                        "font-src 'self' cdn.jsdelivr.net; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'self'; " +
                        "form-action 'self'; " +
                        "base-uri 'self'; "
                    )
                )
                .frameOptions(frame -> frame
                    .deny()
                )
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .permissionsPolicy(permissions -> permissions
                    .policy(
                        "geolocation=(), " +
                        "microphone=(), " +
                        "camera=(), " +
                        "payment=()"
                    )
                )
            );

        return http.build();
    }
}
