package com.samjhadoo.model.content;

import com.samjhadoo.model.audit.DateAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "content_tags")
public class ContentTag extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(unique = true, length = 60)
    private String slug;
    
    @Column(length = 200)
    private String description;
    
    @ManyToMany(mappedBy = "tags")
    private Set<Content> contents = new HashSet<>();
    
    @Column(name = "content_count", nullable = false)
    private int contentCount = 0;
    
    @Column(name = "is_featured", nullable = false)
    private boolean featured = false;
    
    @Column(name = "seo_title", length = 100)
    private String seoTitle;
    
    @Column(name = "meta_description", length = 300)
    private String metaDescription;
}
