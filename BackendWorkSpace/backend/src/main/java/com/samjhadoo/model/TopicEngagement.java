package com.samjhadoo.model.topic;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks user engagement with topics (views, clicks, sessions booked)
 */
@Data
@Entity
@Table(name = "topic_engagements")
public class TopicEngagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private boolean viewed = false;

    @Column(nullable = false)
    private boolean clicked = false;

    @Column(nullable = false)
    private boolean sessionBooked = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime engagedAt;
}
