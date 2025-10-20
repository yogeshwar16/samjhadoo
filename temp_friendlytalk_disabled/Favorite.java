package com.samjhadoo.model.friendlytalk;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "favorite_user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "favorite_user_id", nullable = false)
    private User favoriteUser;

    @Column(name = "tag", length = 50)
    private String tag; // e.g., "Career", "Calm Vibes", "Always Replies"

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "notify_when_online")
    private Boolean notifyWhenOnline = true;

    @Column(name = "is_mutual")
    private Boolean isMutual = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
