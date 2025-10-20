package com.samjhadoo.service;

import com.samjhadoo.config.WebRTCConfig;
import com.samjhadoo.dto.response.WebRTCSessionResponse;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.Session;
import com.samjhadoo.model.User;
import com.samjhadoo.model.WebRTCSession;
import com.samjhadoo.repository.SessionRepository;
import com.samjhadoo.repository.UserRepository;
import com.samjhadoo.repository.WebRTCSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebRTCService {

    private final SimpMessagingTemplate messagingTemplate;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final WebRTCSessionRepository webRTCSessionRepository;

    @Value("${webrtc.stun.servers:stun:stun.l.google.com:19302,stun:stun1.l.google.com:19302}")
    private String[] stunServers;

    @Value("${webrtc.turn.servers:}")
    private String[] turnServers;

    @Value("${webrtc.turn.username:}")
    private String turnUsername;

    @Value("${webrtc.turn.credential:}")
    private String turnCredential;

    @Transactional
    public void handleSignalingMessage(String sessionId, String senderId, Object message) {
        // Verify the session exists and the sender is a participant
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!isParticipant(session, senderId)) {
            throw new SecurityException("User is not a participant in this session");
        }

        // Determine the recipient (the other participant)
        String recipientId = session.getMentor().getId().equals(senderId) ? 
                session.getMentee().getId() : session.getMentor().getId();

        // Forward the signaling message to the recipient
        messagingTemplate.convertAndSendToUser(
                recipientId,
                "/queue/signal",
                message
        );
    }

    @Transactional
    public WebRTCSession createOrGetSession(String sessionId, String userId) {
        return webRTCSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseGet(() -> {
                    WebRTCSession webrtcSession = new WebRTCSession();
                    webrtcSession.setId(UUID.randomUUID().toString());
                    webrtcSession.setSessionId(sessionId);
                    webrtcSession.setUserId(userId);
                    return webRTCSessionRepository.save(webrtcSession);
                });
    }

    @Transactional
    public void saveICECandidate(String sessionId, String userId, String candidate) {
        WebRTCSession webrtcSession = webRTCSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseGet(() -> createOrGetSession(sessionId, userId));
        
        webrtcSession.addIceCandidate(candidate);
        webRTCSessionRepository.save(webrtcSession);
    }

    @Transactional
    public void saveSDP(String sessionId, String userId, String sdp) {
        WebRTCSession webrtcSession = webRTCSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseGet(() -> createOrGetSession(sessionId, userId));
        
        webrtcSession.setSdpOffer(sdp);
        webRTCSessionRepository.save(webrtcSession);
    }

    @Transactional
    public WebRTCSessionResponse getSessionInfo(String sessionId, String userId, boolean isInitiator) {
        WebRTCSession session = webRTCSessionRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseGet(() -> createOrGetSession(sessionId, userId));

        List<WebRTCSessionResponse.IceServer> iceServers = getIceServers();

        return WebRTCSessionResponse.builder()
                .sessionId(session.getSessionId())
                .userId(session.getUserId())
                .sdpOffer(session.getSdpOffer())
                .iceCandidates(session.getIceCandidates())
                .isInitiator(isInitiator)
                .iceServers(iceServers)
                .build();
    }

    @Transactional
    public void cleanupSession(String sessionId, String userId) {
        webRTCSessionRepository.findBySessionIdAndUserId(sessionId, userId).ifPresent(session -> {
            webRTCSessionRepository.delete(session);
            log.info("Cleaned up WebRTC session {} for user {}", sessionId, userId);
        });
    }

    private List<WebRTCSessionResponse.IceServer> getIceServers() {
        List<WebRTCSessionResponse.IceServer> iceServers = new ArrayList<>();

        // Add STUN servers
        for (String stunServer : stunServers) {
            if (!stunServer.trim().isEmpty()) {
                iceServers.add(WebRTCSessionResponse.IceServer.builder()
                        .urls(List.of(stunServer.trim()))
                        .build());
            }
        }

        // Add TURN servers if configured
        if (turnServers != null && turnServers.length > 0 && 
            !turnUsername.isEmpty() && !turnCredential.isEmpty()) {
            for (String turnServer : turnServers) {
                if (!turnServer.trim().isEmpty()) {
                    iceServers.add(WebRTCSessionResponse.IceServer.builder()
                            .urls(List.of(turnServer.trim()))
                            .username(turnUsername)
                            .credential(turnCredential)
                            .build());
                }
            }
        }

        return iceServers;
    }

    private boolean isParticipant(Session session, String userId) {
        return session.getMentor().getId().equals(userId) || 
               session.getMentee().getId().equals(userId);
    }
}
