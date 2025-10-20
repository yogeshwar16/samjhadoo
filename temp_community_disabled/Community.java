package com.samjhadoo.model.community;

import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.CommunityType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "communities")
public class Community {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunityType type;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_private")
    private boolean isPrivate = false;
    
    @Column(name = "requires_approval")
    private boolean requiresApproval = false;
    
    @ManyToMany
    @JoinTable(
        name = "community_members",
        joinColumns = @JoinColumn(name = "community_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @Column(name = "created_at", updatable = false)
    private Long createdAt = System.currentTimeMillis();
    
    @Column(name = "updated_at")
    private Long updatedAt = System.currentTimeMillis();
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }
}
