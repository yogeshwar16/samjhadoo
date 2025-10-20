package com.samjhadoo.dto.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.samjhadoo.model.content.ContentCategory;
import com.samjhadoo.model.content.ContentTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String body;
    private String featuredImage;
    private String authorName;
    private Long authorId;
    private String authorAvatar;
    private ContentCategoryDTO category;
    private Set<ContentTagDTO> tags;
    private String status;
    private boolean featured;
    private boolean allowComments;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private int shareCount;
    private boolean likedByCurrentUser;
    private boolean bookmarkedByCurrentUser;
    private String readingTime;
    private String seoTitle;
    private String metaDescription;
    private String ogImageUrl;
    private boolean promoted;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant publishedAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant promotionEndsAt;
    
    // For content creation/update
    private Long categoryId;
    private Set<Long> tagIds;
    
    // For admin/editor
    private boolean archived;
    private String authorNotes;
    private String seoKeywords;
    private String canonicalUrl;
    private String metaRobots;
    private String language;
    private String template;
    private String customFields;
}
