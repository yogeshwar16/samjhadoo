package com.samjhadoo.repository;

import com.samjhadoo.model.Review;
import com.samjhadoo.model.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);
}
