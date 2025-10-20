package com.samjhadoo.service;

import com.samjhadoo.dto.user.UserDTO;
import com.samjhadoo.dto.ReviewDTO;
import com.samjhadoo.exception.ResourceNotFoundException;
import com.samjhadoo.model.Review;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.ReviewStatus;
import com.samjhadoo.model.enums.VerificationStatus;
import com.samjhadoo.repository.ReviewRepository;
import com.samjhadoo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserDTO);
    }

    @Override
    @Transactional
    public void approveUser(Long userId) {
        User user = findUserById(userId);
        user.setVerificationStatus(VerificationStatus.VERIFIED);
        userRepository.save(user);
        // TODO: Send notification to user
    }

    @Override
    @Transactional
    public void suspendUser(Long userId, String reason) {
        User user = findUserById(userId);
        user.setVerificationStatus(VerificationStatus.SUSPENDED);
        // TODO: Log the reason for suspension
        userRepository.save(user);
        // TODO: Send notification to user
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .verificationStatus(user.getVerificationStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getPendingReviews(Pageable pageable) {
        return reviewRepository.findByStatus(ReviewStatus.PENDING, pageable).map(this::toReviewDTO);
    }

    @Override
    @Transactional
    public void approveReview(Long reviewId) {
        Review review = findReviewById(reviewId);
        review.setStatus(ReviewStatus.APPROVED);
        reviewRepository.save(review);
        // TODO: Send notification to mentee
    }

    @Override
    @Transactional
    public void rejectReview(Long reviewId, String reason) {
        Review review = findReviewById(reviewId);
        review.setStatus(ReviewStatus.REJECTED);
        review.setRejectionReason(reason);
        reviewRepository.save(review);
        // TODO: Send notification to mentee
    }

    private Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
    }

    private ReviewDTO toReviewDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .mentorId(review.getMentor().getId())
                .mentorName(review.getMentor().getFullName())
                .menteeId(review.getMentee().getId())
                .menteeName(review.getMentee().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
