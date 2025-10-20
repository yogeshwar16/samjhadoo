package com.samjhadoo.model.topic;

import com.samjhadoo.model.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks which topics mentors have adopted
 */
@Data
@Entity
@Table(name = "mentor_topic_adoptions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"mentor_id", "topic_id"})
})
public class MentorTopicAdoption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime adoptedAt;
}
