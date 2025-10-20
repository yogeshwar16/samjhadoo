package com.samjhadoo.controller.api.friendlytalk;

import com.samjhadoo.dto.friendlytalk.FavoriteDTO;
import com.samjhadoo.model.User;
import com.samjhadoo.service.friendlytalk.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Manage favorite mentors and friends in Friendly Talk")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    @Operation(summary = "Add a favorite", description = "Add a user to your favorites list")
    public ResponseEntity<FavoriteDTO> addFavorite(
            @Valid @RequestBody FavoriteDTO.CreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        FavoriteDTO favorite = favoriteService.addFavorite(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(favorite);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a favorite", description = "Update favorite details (tag, notes, notifications)")
    public ResponseEntity<FavoriteDTO> updateFavorite(
            @PathVariable Long id,
            @Valid @RequestBody FavoriteDTO.UpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        FavoriteDTO favorite = favoriteService.updateFavorite(id, request, currentUser.getId());
        return ResponseEntity.ok(favorite);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a favorite by ID", description = "Remove a user from your favorites list")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        favoriteService.removeFavorite(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Remove a favorite by user ID", description = "Remove a specific user from your favorites")
    public ResponseEntity<Void> removeFavoriteByUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        
        favoriteService.removeFavoriteByUser(userId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get favorite by ID", description = "Retrieve a specific favorite entry")
    public ResponseEntity<FavoriteDTO> getFavorite(@PathVariable Long id) {
        FavoriteDTO favorite = favoriteService.getFavoriteById(id);
        return ResponseEntity.ok(favorite);
    }

    @GetMapping
    @Operation(summary = "Get all favorites", description = "Retrieve all favorites for the current user")
    public ResponseEntity<List<FavoriteDTO>> getUserFavorites(
            @AuthenticationPrincipal User currentUser) {
        
        List<FavoriteDTO> favorites = favoriteService.getUserFavorites(currentUser.getId());
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "Get favorites by tag", description = "Filter favorites by tag (Career, Calm Vibes, etc.)")
    public ResponseEntity<List<FavoriteDTO>> getFavoritesByTag(
            @PathVariable String tag,
            @AuthenticationPrincipal User currentUser) {
        
        List<FavoriteDTO> favorites = favoriteService.getFavoritesByTag(currentUser.getId(), tag);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all favorite tags", description = "Retrieve all unique tags used in favorites")
    public ResponseEntity<List<String>> getFavoriteTags(
            @AuthenticationPrincipal User currentUser) {
        
        List<String> tags = favoriteService.getUserFavoriteTags(currentUser.getId());
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/mutual")
    @Operation(summary = "Get mutual favorites", description = "Retrieve favorites where both users have favorited each other")
    public ResponseEntity<List<FavoriteDTO>> getMutualFavorites(
            @AuthenticationPrincipal User currentUser) {
        
        List<FavoriteDTO> favorites = favoriteService.getMutualFavorites(currentUser.getId());
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/check/{userId}")
    @Operation(summary = "Check if user is favorited", description = "Check if a specific user is in your favorites")
    public ResponseEntity<Boolean> isFavorite(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        
        boolean isFavorite = favoriteService.isFavorite(currentUser.getId(), userId);
        return ResponseEntity.ok(isFavorite);
    }

    @GetMapping("/count")
    @Operation(summary = "Get favorite count", description = "Get the number of times the current user has been favorited")
    public ResponseEntity<Long> getFavoriteCount(
            @AuthenticationPrincipal User currentUser) {
        
        long count = favoriteService.getFavoriteCount(currentUser.getId());
        return ResponseEntity.ok(count);
    }
}
