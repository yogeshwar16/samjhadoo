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
@Table(name = "languages")
public class Language extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 10)
    private String code; // ISO 639-1 code (e.g., "en", "hi", "ta")
    
    @Column(nullable = false, unique = true, length = 100)
    private String name; // English name (e.g., "English", "Hindi", "Tamil")
    
    @Column(name = "native_name", nullable = false, length = 100)
    private String nativeName; // Native name (e.g., "English", "हिन्दी", "தமிழ்")
    
    @Column(name = "is_rtl", nullable = false)
    private boolean rtl = false; // Right-to-left language
    
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
    
    @Column(name = "is_default", nullable = false)
    private boolean defaultLanguage = false;
    
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
    
    @Column(name = "flag_emoji", length = 10)
    private String flagEmoji; // 🇮🇳, 🇺🇸, etc.
    
    @Column(name = "locale_code", length = 10)
    private String localeCode; // en_US, hi_IN, ta_IN
    
    @Column(name = "completion_percentage")
    private Double completionPercentage = 0.0; // Translation completion %
}
