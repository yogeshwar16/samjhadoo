package com.samjhadoo.repository.visualquery;

import com.samjhadoo.model.visualquery.VisualQuery;
import com.samjhadoo.model.User;
import com.samjhadoo.model.enums.visualquery.QueryCategory;
import com.samjhadoo.model.enums.visualquery.QueryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisualQueryRepository extends JpaRepository<VisualQuery, Long> {

    List<VisualQuery> findByUser(User user);

    List<VisualQuery> findByStatus(QueryStatus status);

    List<VisualQuery> findByCategory(QueryCategory category);

    List<VisualQuery> findByAssignedMentor(User mentor);

    List<VisualQuery> findByIsPublicTrue();

    List<VisualQuery> findByAnonymousTrue();

    @Query("SELECT vq FROM VisualQuery vq WHERE vq.status = 'SUBMITTED' ORDER BY vq.urgencyLevel DESC, vq.submittedAt ASC")
    List<VisualQuery> findSubmittedQueries();

    @Query("SELECT vq FROM VisualQuery vq WHERE vq.status = 'IN_PROGRESS' AND vq.assignedMentor = :mentor ORDER BY vq.urgencyLevel DESC, vq.submittedAt ASC")
    List<VisualQuery> findInProgressByMentor(@Param("mentor") User mentor);

    @Query("SELECT vq FROM VisualQuery vq WHERE vq.status IN ('SUBMITTED', 'UNDER_REVIEW') AND vq.responseDeadline < :now ORDER BY vq.responseDeadline ASC")
    List<VisualQuery> findOverdueQueries(@Param("now") LocalDateTime now);

    @Query("SELECT vq FROM VisualQuery vq WHERE vq.tags LIKE %:tag% AND vq.status IN ('SUBMITTED', 'UNDER_REVIEW', 'IN_PROGRESS') ORDER BY vq.createdAt DESC")
    List<VisualQuery> findByTag(@Param("tag") String tag);

    @Query("SELECT vq FROM VisualQuery vq WHERE vq.title LIKE %:keyword% OR vq.description LIKE %:keyword% ORDER BY vq.createdAt DESC")
    List<VisualQuery> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT vq FROM VisualQuery vq WHERE vq.urgencyLevel >= :minUrgency ORDER BY vq.urgencyLevel DESC, vq.submittedAt ASC")
    List<VisualQuery> findHighUrgencyQueries(@Param("minUrgency") int minUrgency);

    @Query("SELECT vq FROM VisualQuery vq WHERE vq.submittedAt >= :since ORDER BY vq.submittedAt DESC")
    List<VisualQuery> findRecentQueries(@Param("since") LocalDateTime since);

    @Query("SELECT vq FROM VisualQuery vq WHERE vq.resolvedAt IS NOT NULL ORDER BY vq.resolutionRating DESC")
    List<VisualQuery> findResolvedQueries();

    @Query("SELECT COUNT(vq) FROM VisualQuery vq WHERE vq.status = 'SUBMITTED'")
    long countSubmittedQueries();

    @Query("SELECT COUNT(vq) FROM VisualQuery vq WHERE vq.status = 'IN_PROGRESS'")
    long countInProgressQueries();

    @Query("SELECT COUNT(vq) FROM VisualQuery vq WHERE vq.status = 'RESOLVED'")
    long countResolvedQueries();

    @Query("SELECT AVG(vq.resolutionRating) FROM VisualQuery vq WHERE vq.resolutionRating > 0")
    Double getAverageResolutionRating();

    @Query("SELECT vq FROM VisualQuery vq WHERE vq.viewCount >= :minViews ORDER BY vq.viewCount DESC")
    List<VisualQuery> findPopularQueries(@Param("minViews") int minViews);

    @Query("SELECT vq FROM VisualQuery vq WHERE vq.helpfulVotes >= :minVotes ORDER BY vq.helpfulVotes DESC")
    List<VisualQuery> findHelpfulQueries(@Param("minVotes") int minVotes);

    @Query("SELECT vq FROM VisualQuery vq WHERE vq.assignedMentor IS NULL AND vq.status = 'SUBMITTED' ORDER BY vq.urgencyLevel DESC, vq.submittedAt ASC")
    List<VisualQuery> findUnassignedQueries();

    @Query("SELECT vq FROM VisualQuery vq WHERE vq.allowMentorBidding = true AND vq.status = 'SUBMITTED' ORDER BY vq.createdAt ASC")
    List<VisualQuery> findBiddableQueries();
}
