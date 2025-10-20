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
@Table(name = "room_moderators")
public class RoomModerator extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private FriendlyRoom room;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "added_by", nullable = false)
    private String addedBy;
    
    @Column(name = "added_at", nullable = false)
    private Instant addedAt;
    
    @Column(name = "permissions", length = 1000)
    private String permissions; // JSON string of specific permissions
    
    // Helper methods
    public boolean hasPermission(String permission) {
        // Simple contains check - in a real app, you'd parse the JSON and check specific permissions
        return permissions != null && permissions.contains(permission);
    }
}
