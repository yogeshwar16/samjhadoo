package com.samjhadoo.controller.api.content;

import com.samjhadoo.dto.content.ContentCategoryDTO;
import com.samjhadoo.dto.content.ContentDTO;
import com.samjhadoo.dto.content.ContentTagDTO;
import com.samjhadoo.security.CurrentUser;
import com.samjhadoo.security.UserPrincipal;
import com.samjhadoo.service.content.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/content")
@Tag(name = "Content Hub", description = "Content management and discovery")
public class ContentController {

    private final ContentService contentService;

    @GetMapping
    @Operation(summary = "Get all content", description = "Retrieves a paginated list of content with filtering options")
    public ResponseEntity<Page<ContentDTO>> getAllContent(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10) Pageable pageable,
            @CurrentUser UserPrincipal currentUser) {
        
        Page<ContentDTO> content = contentService.getAllContent(
            pageable, status, categoryId, tag, query, currentUser);
        return ResponseEntity.ok(content);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get content by slug", description = "Retrieves a single content item by its slug")
    public ResponseEntity<ContentDTO> getContentBySlug(
            @PathVariable String slug,
            @CurrentUser UserPrincipal currentUser) {
        
        ContentDTO content = contentService.getContentBySlug(slug, currentUser);
        return ResponseEntity.ok(content);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create content", description = "Creates a new content item")
    public ResponseEntity<ContentDTO> createContent(
            @Valid @RequestBody ContentDTO contentDTO,
            @CurrentUser UserPrincipal currentUser) {
        
        ContentDTO createdContent = contentService.createContent(contentDTO, currentUser);
        return ResponseEntity.ok(createdContent);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update content", description = "Updates an existing content item")
    public ResponseEntity<ContentDTO> updateContent(
            @PathVariable Long id,
            @Valid @RequestBody ContentDTO contentDTO,
            @CurrentUser UserPrincipal currentUser) {
        
        ContentDTO updatedContent = contentService.updateContent(id, contentDTO, currentUser);
        return ResponseEntity.ok(updatedContent);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @contentSecurity.canEditContent(authentication, #id)")
    @Operation(summary = "Delete content", description = "Deletes a content item (admin or owner only)")
    public ResponseEntity<Void> deleteContent(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        contentService.deleteContent(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Retrieves all content categories")
    public ResponseEntity<List<ContentCategoryDTO>> getAllCategories() {
        List<ContentCategoryDTO> categories = contentService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{slug}")
    @Operation(summary = "Get category by slug", description = "Retrieves a category by its slug")
    public ResponseEntity<ContentCategoryDTO> getCategoryBySlug(@PathVariable String slug) {
        ContentCategoryDTO category = contentService.getCategoryBySlug(slug);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all tags", description = "Retrieves all content tags")
    public ResponseEntity<List<ContentTagDTO>> getAllTags() {
        List<ContentTagDTO> tags = contentService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Like/Unlike content", description = "Toggles like on a content item")
    public ResponseEntity<ContentDTO> toggleLike(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        ContentDTO content = contentService.toggleLike(id, currentUser);
        return ResponseEntity.ok(content);
    }

    @PostMapping("/{id}/bookmark")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Bookmark content", description = "Toggles bookmark on a content item")
    public ResponseEntity<ContentDTO> toggleBookmark(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        ContentDTO content = contentService.toggleBookmark(id, currentUser);
        return ResponseEntity.ok(content);
    }

    @GetMapping("/search")
    @Operation(summary = "Search content", description = "Searches content by query string")
    public ResponseEntity<Page<ContentDTO>> searchContent(
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<ContentDTO> results = contentService.searchContent(query, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "Get related content", description = "Gets content related to the specified item")
    public ResponseEntity<List<ContentDTO>> getRelatedContent(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<ContentDTO> related = contentService.getRelatedContent(id, limit);
        return ResponseEntity.ok(related);
    }

    @GetMapping("/recommended")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get recommended content", description = "Gets personalized content recommendations")
    public ResponseEntity<List<ContentDTO>> getRecommendedContent(
            @RequestParam(defaultValue = "5") int limit,
            @CurrentUser UserPrincipal currentUser) {
        
        List<ContentDTO> recommended = contentService.getRecommendedContent(currentUser, limit);
        return ResponseEntity.ok(recommended);
    }
}
