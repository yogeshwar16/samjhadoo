package com.samjhadoo.repository;

import com.samjhadoo.model.MentorProfile;
import com.samjhadoo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, String> {
    Optional<MentorProfile> findByUser(User user);
    boolean existsByUser(User user);
    
    @Query("SELECT m FROM MentorProfile m WHERE " +
           "(:skill IS NULL OR LOWER(m.expertise) LIKE LOWER(concat('%', :skill, '%'))) AND " +
           "(:minRate IS NULL OR m.hourlyRate >= :minRate) AND " +
           "(:maxRate IS NULL OR m.hourlyRate <= :maxRate) AND " +
           "m.isAvailable = true")
    List<MentorProfile> searchMentors(
        @Param("skill") String skill,
        @Param("minRate") Double minRate,
        @Param("maxRate") Double maxRate
    );
}
