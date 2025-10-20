package com.samjhadoo.model.content;

import com.samjhadoo.model.User;
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
@Table(name = "contents")
public class Content extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String body;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ContentCategory category;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "content_tags",
        joinColumns = @JoinColumn(name = "content_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<ContentTag> tags = new HashSet<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentStatus status = ContentStatus.DRAFT;
    
    @Column(name = "is_featured", nullable = false)
    private boolean featured = false;
    
    @Column(name = "allow_comments", nullable = false)
    private boolean allowComments = true;
    
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;
    
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;
    
    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;
    
    @Column(name = "share_count", nullable = false)
    private int shareCount = 0;
    
    @Column(name = "seo_title", length = 100)
    private String seoTitle;
    
    @Column(name = "meta_description", length = 300)
    private String metaDescription;
    
    @Column(name = "og_image_url")
    private String ogImageUrl;
    
    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;
    
    @Column(name = "is_promoted", nullable = false)
    private boolean promoted = false;
    
    @Column(name = "promotion_ends_at")
    private Long promotionEndsAt;
    
    @Column(name = "slug", unique = true, length = 120)
    private String slug;
    
    // Enums
    public enum ContentStatus {
        DRAFT, PUBLISHED, SCHEDULED, ARCHIVED
    }
}
