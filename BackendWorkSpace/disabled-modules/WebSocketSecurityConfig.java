package com.samjhadoo.config.security;

import com.samjhadoo.config.websocket.WebSocketProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.List;

@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
@RequiredArgsConstructor
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    private final JwtDecoder jwtDecoder;
    private final WebSocketProperties webSocketProperties;
    private final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            // Public endpoints (no auth required)
            .simpSubscribeDestMatchers(
                "/topic/public/**",
                "/queue/public/**"
            ).permitAll()
            
            // User-specific endpoints (require authentication)
            .simpSubscribeDestMatchers(
                "/user/queue/**",
                "/user/topic/**"
            ).authenticated()
            
            // Community-specific endpoints (require authentication)
            .simpSubscribeDestMatchers(
                "/topic/communities/*",
                "/queue/communities/*"
            ).access("@webSocketSecurity.checkCommunityAccess(authentication, #communityId)")
            
            // Default deny
            .anyMessage().denyAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        // Disable CSRF for WebSocket for now (handled by STOMP headers)
        return true;
    }

    @Override
    public void customizeClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                    message, StompHeaderAccessor.class);
                
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        try {
                            String token = authHeader.substring(7);
                            Jwt jwt = jwtDecoder.decode(token);
                            Authentication authentication = jwtAuthenticationConverter.convert(jwt);
                            
                            if (authentication != null) {
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                accessor.setUser(authentication);
                                
                                log.debug("Authenticated WebSocket connection for user: {}", 
                                    authentication.getName());
                            }
                        } catch (JwtException e) {
                            log.warn("Failed to authenticate WebSocket connection: {}", e.getMessage());
                            throw new SecurityException("Invalid token");
                        }
                    } else {
                        log.warn("No JWT token found in WebSocket headers");
                        throw new SecurityException("No authorization token provided");
                    }
                }
                
                return message;
            }
        });
    }

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages, ChannelRegistration registration) {
        super.configureInbound(messages);
        
        // Add rate limiting interceptor
        registration.interceptors(new RateLimitingInterceptor());
    }
}

/**
 * Rate limiting interceptor for WebSocket messages
 */
@Slf4j
class RateLimitingInterceptor implements ChannelInterceptor {
    
    // TODO: Implement rate limiting logic using Redis or similar
    // This is a basic example - replace with a production-ready solution
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message, StompHeaderAccessor.class);
            
        if (accessor != null && accessor.getUser() != null) {
            String username = accessor.getUser().getName();
            // Check rate limit for user
            if (isRateLimited(username)) {
                log.warn("Rate limit exceeded for user: {}", username);
                throw new SecurityException("Rate limit exceeded");
            }
        }
        
        return message;
    }
    
    private boolean isRateLimited(String username) {
        // Implement rate limiting logic here
        // This is a placeholder - use Redis or similar in production
        return false;
    }
}
