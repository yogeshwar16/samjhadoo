package com.samjhadoo.repository;

import com.samjhadoo.model.community.Community;
import com.samjhadoo.model.enums.CommunityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    
    List<Community> findByType(CommunityType type);
    
    List<Community> findByIsPrivateFalse();
    
    List<Community> findByNameContainingIgnoreCase(String name);
    
    List<Community> findByMembersId(Long userId);
    
    @Query("SELECT c FROM Community c WHERE c.isActive = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Community> search(@Param("query") String query);
    
    @Query("SELECT c FROM Community c WHERE c.isActive = true AND c.isPrivate = false " +
           "AND c.id NOT IN (SELECT cm.community.id FROM CommunityMember cm WHERE cm.user.id = :userId)")
    List<Community> findRecommendedCommunities(@Param("userId") Long userId);
    
    boolean existsByName(String name);
}
