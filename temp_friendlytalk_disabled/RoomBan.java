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
@Table(name = "room_bans")
public class RoomBan extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private FriendlyRoom room;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "banned_by", nullable = false)
    private String bannedBy;
    
    @Column(length = 500)
    private String reason;
    
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
    @Column(name = "is_permanent", nullable = false)
    private boolean isPermanent = false;
    
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
    
    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }
    
    public void setPermanent(boolean permanent) {
        isPermanent = permanent;
        if (permanent) {
            expiresAt = null; // Null means never expires
        } else if (expiresAt == null) {
            expiresAt = Instant.now().plus(java.time.Duration.ofDays(7)); // Default 7-day ban
        }
    }
}
