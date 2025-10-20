package com.samjhadoo.model;

import com.samjhadoo.model.enums.CommunityTag;
import com.samjhadoo.model.enums.CommunityType;
import com.samjhadoo.model.enums.Role;
import com.samjhadoo.model.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String fullName;
    
    @Enumerated(EnumType.STRING)
    private CommunityType communityType;
    
    @Enumerated(EnumType.STRING)
    private CommunityTag communityTag; // Student, Farmer, Employee, Woman, Senior Citizen
    
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;
    
    private String phoneNumber;
    private String profileImageUrl;
    private String bio;
    private String location;
    private String language;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }
    
    // Helper methods for backward compatibility
    public String getFirstName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+");
            return parts[0];
        }
        return "";
    }
    
    public String getLastName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length > 1) {
                return String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
            }
        }
        return "";
    }
    
    // Default session type method - placeholder for missing functionality
    public String getDefaultSessionType() {
        return "VIDEO"; // Default session type
    }
}
