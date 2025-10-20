package com.samjhadoo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRTCSessionResponse {
    private String sessionId;
    private String userId;
    private String sdpOffer;
    private List<String> iceCandidates;
    private boolean isInitiator;
    
    // ICE Server configuration
    private List<IceServer> iceServers;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IceServer {
        private List<String> urls;
        private String username;
        private String credential;
    }
}
