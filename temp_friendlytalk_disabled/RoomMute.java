package com.samjhadoo.model.friendlytalk;

import com.samjhadoo.model.audit.DateAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "room_mutes")
public class RoomMute extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private FriendlyRoom room;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "muted_by", nullable = false)
    private String mutedBy;
    
    @Column(length = 500)
    private String reason;
    
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }
    
    public long getRemainingSeconds() {
        if (isExpired()) {
            return 0;
        }
        return expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
    }
}
