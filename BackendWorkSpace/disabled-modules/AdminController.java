package com.samjhadoo.controller.api;

import com.samjhadoo.dto.user.UserDTO;
import com.samjhadoo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @PostMapping("/users/{userId}/approve")
    public ResponseEntity<Void> approveUser(@PathVariable Long userId) {
        adminService.approveUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<Void> suspendUser(@PathVariable Long userId, @RequestBody String reason) {
        adminService.suspendUser(userId, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reviews/pending")
    public ResponseEntity<Page<ReviewDTO>> getPendingReviews(Pageable pageable) {
        return ResponseEntity.ok(adminService.getPendingReviews(pageable));
    }

    @PostMapping("/reviews/{reviewId}/approve")
    public ResponseEntity<Void> approveReview(@PathVariable Long reviewId) {
        adminService.approveReview(reviewId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reviews/{reviewId}/reject")
    public ResponseEntity<Void> rejectReview(@PathVariable Long reviewId, @RequestBody String reason) {
        adminService.rejectReview(reviewId, reason);
        return ResponseEntity.ok().build();
    }
}
