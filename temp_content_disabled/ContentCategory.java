package com.samjhadoo.model.content;

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
@Table(name = "content_categories")
public class ContentCategory extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 200)
    private String description;
    
    @Column(unique = true, length = 120)
    private String slug;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ContentCategory parent;
    
    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;
    
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
    
    @Column(name = "seo_title", length = 100)
    private String seoTitle;
    
    @Column(name = "meta_description", length = 300)
    private String metaDescription;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "content_count", nullable = false)
    private int contentCount = 0;
}
