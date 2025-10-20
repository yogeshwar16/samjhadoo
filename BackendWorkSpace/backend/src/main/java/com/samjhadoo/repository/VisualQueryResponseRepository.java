package com.samjhadoo.repository.visualquery;

import com.samjhadoo.model.visualquery.VisualQueryResponse;
import com.samjhadoo.model.visualquery.VisualQuery;
import com.samjhadoo.model.User;
import com.samjhadoo.model.visualquery.VisualQueryResponse.ResponseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisualQueryResponseRepository extends JpaRepository<VisualQueryResponse, Long> {

    List<VisualQueryResponse> findByVisualQuery(VisualQuery visualQuery);

    List<VisualQueryResponse> findByMentor(User mentor);

    List<VisualQueryResponse> findByIsSolutionTrue();

    List<VisualQueryResponse> findByResponseType(ResponseType responseType);

    @Query("SELECT vqr FROM VisualQueryResponse vqr WHERE vqr.visualQuery = :query ORDER BY vqr.createdAt ASC")
    List<VisualQueryResponse> findByQueryOrderByCreated(@Param("query") VisualQuery query);

    @Query("SELECT vqr FROM VisualQueryResponse vqr WHERE vqr.isHelpful = true ORDER BY vqr.helpfulVotes DESC")
    List<VisualQueryResponse> findHelpfulResponses();

    @Query("SELECT vqr FROM VisualQueryResponse vqr WHERE vqr.mentorRating >= :minRating ORDER BY vqr.mentorRating DESC")
    List<VisualQueryResponse> findHighRatedResponses(@Param("minRating") int minRating);

    @Query("SELECT vqr FROM VisualQueryResponse vqr WHERE vqr.helpfulnessRatio >= :minRatio AND vqr.totalVotes >= :minVotes ORDER BY vqr.helpfulnessRatio DESC")
    List<VisualQueryResponse> findHighlyHelpfulResponses(@Param("minRatio") double minRatio, @Param("minVotes") int minVotes);

    @Query("SELECT vqr FROM VisualQueryResponse vqr WHERE vqr.mentor = :mentor ORDER BY vqr.createdAt DESC")
    List<VisualQueryResponse> findByMentorOrderByCreated(@Param("mentor") User mentor);

    @Query("SELECT COUNT(vqr) FROM VisualQueryResponse vqr WHERE vqr.visualQuery = :query")
    long countByQuery(@Param("query") VisualQuery query);

    @Query("SELECT AVG(vqr.helpfulnessRatio) FROM VisualQueryResponse vqr WHERE vqr.totalVotes > 0")
    Double getAverageHelpfulnessRatio();

    @Query("SELECT AVG(vqr.mentorRating) FROM VisualQueryResponse vqr WHERE vqr.mentorRating > 0")
    Double getAverageMentorRating();

    @Query("SELECT vqr FROM VisualQueryResponse vqr WHERE vqr.createdAt >= :since ORDER BY vqr.createdAt DESC")
    List<VisualQueryResponse> findRecentResponses(@Param("since") java.time.LocalDateTime since);

    @Query("SELECT vqr FROM VisualQueryResponse vqr WHERE vqr.isSolution = true ORDER BY vqr.createdAt ASC")
    List<VisualQueryResponse> findSolutionResponses();
}
