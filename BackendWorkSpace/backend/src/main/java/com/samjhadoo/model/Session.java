package com.samjhadoo.model;

import com.samjhadoo.model.enums.SessionStatus;
import com.samjhadoo.model.enums.SessionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sessions")
public class Session {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private User mentee;
    
    @Enumerated(EnumType.STRING)
    private SessionType sessionType;
    
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
    
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String meetingUrl;
    private String meetingId;
    private String meetingPassword;
    private String notes;
    private Double price;
    private String currency;
    private String paymentId;
    private String receiptUrl;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    // Additional methods for business logic can be added here
    public boolean isUpcoming() {
        return status == SessionStatus.SCHEDULED && startTime.isAfter(LocalDateTime.now());
    }
    
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return status == SessionStatus.IN_PROGRESS || 
               (status == SessionStatus.SCHEDULED && 
                now.isAfter(startTime) && 
                now.isBefore(endTime));
    }
    
    public boolean isCompleted() {
        return status == SessionStatus.COMPLETED || 
               (status == SessionStatus.SCHEDULED && 
                endTime.isBefore(LocalDateTime.now()));
    }
}
