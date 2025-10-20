package com.samjhadoo.repository.visualquery;

import com.samjhadoo.model.visualquery.VisualQueryMedia;
import com.samjhadoo.model.visualquery.VisualQuery;
import com.samjhadoo.model.enums.visualquery.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisualQueryMediaRepository extends JpaRepository<VisualQueryMedia, Long> {

    List<VisualQueryMedia> findByVisualQuery(VisualQuery visualQuery);

    List<VisualQueryMedia> findByMediaType(MediaType mediaType);

    List<VisualQueryMedia> findByIsPrimaryTrue();

    @Query("SELECT vqm FROM VisualQueryMedia vqm WHERE vqm.visualQuery = :query ORDER BY vqm.isPrimary DESC, vqm.createdAt ASC")
    List<VisualQueryMedia> findByQueryOrderByPrimary(@Param("query") VisualQuery query);

    @Query("SELECT vqm FROM VisualQueryMedia vqm WHERE vqm.uploadStatus = 'COMPLETED' ORDER BY vqm.createdAt DESC")
    List<VisualQueryMedia> findUploadedMedia();

    @Query("SELECT vqm FROM VisualQueryMedia vqm WHERE vqm.uploadStatus = 'FAILED' ORDER BY vqm.createdAt DESC")
    List<VisualQueryMedia> findFailedUploads();

    @Query("SELECT vqm FROM VisualQueryMedia vqm WHERE vqm.compressionApplied = true ORDER BY vqm.compressedSizeBytes ASC")
    List<VisualQueryMedia> findCompressedMedia();

    @Query("SELECT vqm FROM VisualQueryMedia vqm WHERE vqm.aiAnalysisResult IS NOT NULL ORDER BY vqm.processedAt DESC")
    List<VisualQueryMedia> findAnalyzedMedia();

    @Query("SELECT COUNT(vqm) FROM VisualQueryMedia vqm WHERE vqm.visualQuery = :query")
    long countByQuery(@Param("query") VisualQuery query);

    @Query("SELECT SUM(vqm.fileSizeBytes) FROM VisualQueryMedia vqm WHERE vqm.visualQuery = :query AND vqm.uploadStatus = 'COMPLETED'")
    Long getTotalFileSizeByQuery(@Param("query") VisualQuery query);

    @Query("SELECT AVG(vqm.compressionRatio) FROM VisualQueryMedia vqm WHERE vqm.compressionApplied = true")
    Double getAverageCompressionRatio();

    @Query("SELECT vqm FROM VisualQueryMedia vqm WHERE vqm.fileSizeBytes > :minSize ORDER BY vqm.fileSizeBytes DESC")
    List<VisualQueryMedia> findLargeFiles(@Param("minSize") Long minSize);
}
