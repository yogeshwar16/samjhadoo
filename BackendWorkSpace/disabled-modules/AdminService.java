package com.samjhadoo.service;

import com.samjhadoo.dto.user.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    Page<UserDTO> getAllUsers(Pageable pageable);

    void approveUser(Long userId);

    void suspendUser(Long userId, String reason);

    Page<com.samjhadoo.dto.ReviewDTO> getPendingReviews(Pageable pageable);

    void approveReview(Long reviewId);

    void rejectReview(Long reviewId, String reason);
}
