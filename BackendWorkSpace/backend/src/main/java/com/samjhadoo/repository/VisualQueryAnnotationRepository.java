package com.samjhadoo.repository.visualquery;

import com.samjhadoo.model.visualquery.VisualQueryAnnotation;
import com.samjhadoo.model.visualquery.VisualQueryResponse;
import com.samjhadoo.model.visualquery.VisualQueryMedia;
import com.samjhadoo.model.visualquery.VisualQueryAnnotation.AnnotationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisualQueryAnnotationRepository extends JpaRepository<VisualQueryAnnotation, Long> {

    List<VisualQueryAnnotation> findByResponse(VisualQueryResponse response);

    List<VisualQueryAnnotation> findByMedia(VisualQueryMedia media);

    List<VisualQueryAnnotation> findByAnnotationType(AnnotationType annotationType);

    @Query("SELECT vqa FROM VisualQueryAnnotation vqa WHERE vqa.response = :response ORDER BY vqa.zIndex ASC, vqa.createdAt ASC")
    List<VisualQueryAnnotation> findByResponseOrderByZIndex(@Param("response") VisualQueryResponse response);

    @Query("SELECT vqa FROM VisualQueryAnnotation vqa WHERE vqa.media = :media ORDER BY vqa.zIndex ASC, vqa.createdAt ASC")
    List<VisualQueryAnnotation> findByMediaOrderByZIndex(@Param("media") VisualQueryMedia media);

    @Query("SELECT COUNT(vqa) FROM VisualQueryAnnotation vqa WHERE vqa.response = :response")
    long countByResponse(@Param("response") VisualQueryResponse response);

    @Query("SELECT vqa FROM VisualQueryAnnotation vqa WHERE vqa.text IS NOT NULL AND vqa.text != '' ORDER BY vqa.createdAt DESC")
    List<VisualQueryAnnotation> findTextAnnotations();

    @Query("SELECT vqa FROM VisualQueryAnnotation vqa WHERE vqa.isVisible = true ORDER BY vqa.createdAt DESC")
    List<VisualQueryAnnotation> findVisibleAnnotations();

    @Query("SELECT vqa FROM VisualQueryAnnotation vqa WHERE vqa.annotationType IN ('ARROW', 'CIRCLE', 'RECTANGLE') ORDER BY vqa.createdAt DESC")
    List<VisualQueryAnnotation> findHighlightAnnotations();
}
