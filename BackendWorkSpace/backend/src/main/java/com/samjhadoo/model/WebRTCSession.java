package com.samjhadoo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "webrtc_sessions")
public class WebRTCSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "sdp_offer")
    private String sdpOffer;

    @ElementCollection
    @CollectionTable(name = "webrtc_ice_candidates", joinColumns = @JoinColumn(name = "webrtc_session_id"))
    @Column(name = "candidate")
    private List<String> iceCandidates = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addIceCandidate(String candidate) {
        if (this.iceCandidates == null) {
            this.iceCandidates = new ArrayList<>();
        }
        this.iceCandidates.add(candidate);
    }
}
