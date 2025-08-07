package com.adeprogramming.javagis.repository;
import com.adeprogramming.javagis.domain.ai.AiAnalysisResult;
import com.adeprogramming.javagis.domain.ai.AiWorkflow;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Repository for AiAnalysisResult entity.
 * Provides methods for accessing AI analysis result data.
 */
@Repository
public interface AiAnalysisResultRepository extends JpaRepository<AiAnalysisResult, UUID> {

    /**
     * Find results by name containing the given text.
     *
     * @param name The name to search for
     * @return List of matching results
     */
    List<AiAnalysisResult> findByNameContainingIgnoreCase(String name);

    /**
     * Find results by workflow ID.
     *
     * @param workflowId The ID of the workflow
     * @return List of results for the workflow
     */
    List<AiAnalysisResult> findByWorkflowId(UUID workflowId);

    /**
     * Find results by status.
     *
     * @param status The result status
     * @return List of matching results
     */
    List<AiAnalysisResult> findByStatus(AiAnalysisResult.ResultStatus status);

    /**
     * Find results by execution date range.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching results
     */
    List<AiAnalysisResult> findByExecutionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find results by area of interest intersecting with the given geometry.
     *
     * @param geometry The geometry to check for intersection
     * @return List of matching results
     */
    @Query("SELECT r FROM AiAnalysisResult r WHERE ST_Intersects(r.areaOfInterest, :geometry) = true")
    List<AiAnalysisResult> findByIntersectingGeometry(@Param("geometry") Geometry geometry);

    /**
     * Find results by time range overlapping with the given range.
     *
     * @param start The start of the time range
     * @param end The end of the time range
     * @return List of matching results
     */
    @Query("SELECT r FROM AiAnalysisResult r WHERE " +
            "(r.timeRangeStart <= :end AND r.timeRangeEnd >= :start)")
    List<AiAnalysisResult> findByOverlappingTimeRange(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Find results with confidence score greater than or equal to the given value.
     *
     * @param confidenceScore The minimum confidence score
     * @return List of matching results
     */
    List<AiAnalysisResult> findByConfidenceScoreGreaterThanEqual(Double confidenceScore);

    /**
     * Find the latest result for a workflow.
     *
     * @param workflowId The ID of the workflow
     * @return The latest result, if any
     */
    @Query("SELECT r FROM AiAnalysisResult r WHERE r.workflow.id = :workflowId " +
            "ORDER BY r.executionDate DESC")
    List<AiAnalysisResult> findLatestByWorkflowId(@Param("workflowId") UUID workflowId);

    Arrays findByExecutionDateBetween(AiWorkflow workflow);

    List<AiAnalysisResult> findByWorkflowOrderByExecutionDateDesc(AiWorkflow workflow);
}
