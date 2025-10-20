package com.samjhadoo.model.localization;

import com.samjhadoo.model.audit.DateAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "translations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"translation_key", "language_id"}))
public class Translation extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "translation_key", nullable = false, length = 200)
    private String key; // e.g., "common.welcome", "auth.login.button"
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String value; // Translated text
    
    @Column(name = "category", length = 100)
    private String category; // e.g., "common", "auth", "profile", "errors"
    
    @Column(name = "context", columnDefinition = "TEXT")
    private String context; // Context for translators
    
    @Column(name = "is_reviewed", nullable = false)
    private boolean reviewed = false;
    
    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;
    
    @Column(name = "reviewer_notes", columnDefinition = "TEXT")
    private String reviewerNotes;
    
    @Column(name = "plural_form")
    private String pluralForm; // For languages with plural rules
    
    @Column(name = "placeholder_info", columnDefinition = "TEXT")
    private String placeholderInfo; // Info about {placeholders}
}
