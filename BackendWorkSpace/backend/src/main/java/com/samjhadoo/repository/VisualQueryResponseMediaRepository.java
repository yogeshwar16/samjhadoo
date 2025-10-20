package com.samjhadoo.repository.visualquery;

import com.samjhadoo.model.visualquery.VisualQueryResponseMedia;
import com.samjhadoo.model.visualquery.VisualQueryResponse;
import com.samjhadoo.model.enums.visualquery.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisualQueryResponseMediaRepository extends JpaRepository<VisualQueryResponseMedia, Long> {

    List<VisualQueryResponseMedia> findByResponse(VisualQueryResponse response);

    List<VisualQueryResponseMedia> findByMediaType(MediaType mediaType);

    List<VisualQueryResponseMedia> findByIsDemoTrue();

    List<VisualQueryResponseMedia> findByIsSolutionMediaTrue();

    @Query("SELECT vqrm FROM VisualQueryResponseMedia vqrm WHERE vqrm.response = :response ORDER BY vqrm.stepNumber ASC, vqrm.createdAt ASC")
    List<VisualQueryResponseMedia> findByResponseOrderByStep(@Param("response") VisualQueryResponse response);

    @Query("SELECT vqrm FROM VisualQueryResponseMedia vqrm WHERE vqrm.stepNumber IS NOT NULL ORDER BY vqrm.stepNumber ASC")
    List<VisualQueryResponseMedia> findStepByStepMedia();

    @Query("SELECT COUNT(vqrm) FROM VisualQueryResponseMedia vqrm WHERE vqrm.response = :response")
    long countByResponse(@Param("response") VisualQueryResponse response);

    @Query("SELECT vqrm FROM VisualQueryResponseMedia vqrm WHERE vqrm.fileSizeBytes > :minSize ORDER BY vqrm.fileSizeBytes DESC")
    List<VisualQueryResponseMedia> findLargeResponseMedia(@Param("minSize") Long minSize);
}
