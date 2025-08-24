package com.adeprogramming.javagis.repository;

import com.adeprogramming.javagis.domain.ai.AiWorkflow;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AiWorkflow entity.
 * Provides methods for accessing AI workflow data.
 */
@Repository
public interface AiWorkflowRepository extends JpaRepository<AiWorkflow, UUID> {

    /**
     * Find workflows by name containing the given text.
     *
     * @param name The name to search for
     * @return List of matching workflows
     */
    List<AiWorkflow> findByNameContainingIgnoreCase(String name);

    /**
     * Find workflows by type.
     *
     * @param type The workflow type
     * @return List of matching workflows
     */
    List<AiWorkflow> findByType(AiWorkflow.WorkflowType type);

    /**
     * Find workflows by status.
     *
     * @param status The workflow status
     * @return List of matching workflows
     */
    List<AiWorkflow> findByStatus(AiWorkflow.WorkflowStatus status);

    /**
     * Find active workflows.
     *
     * @return List of active workflows
     */
    List<AiWorkflow> findByIsActiveTrue();

    /**
     * Find workflows scheduled to run before the given date.
     *
     * @param dateTime The date to check against
     * @return List of workflows scheduled to run
     */
    List<AiWorkflow> findByNextScheduledRunBeforeAndStatusAndIsActiveTrue(
            LocalDateTime dateTime, AiWorkflow.WorkflowStatus status);

    /**
     * Find workflows by area of interest intersecting with the given geometry.
     *
     * @param geometry The geometry to check for intersection
     * @return List of matching workflows
     */
    @Query("SELECT w FROM AiWorkflow w WHERE ST_Intersects(w.areaOfInterest, :geometry) = true")
    List<AiWorkflow> findByIntersectingGeometry(@Param("geometry") Geometry geometry);

    /**
     * Find workflows by time range overlapping with the given range.
     *
     * @param start The start of the time range
     * @param end The end of the time range
     * @return List of matching workflows
     */
    @Query("SELECT w FROM AiWorkflow w WHERE " +
            "(w.timeRangeStart <= :end AND w.timeRangeEnd >= :start)")
    List<AiWorkflow> findByOverlappingTimeRange(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Find workflows by model ID.
     *
     * @param modelId The ID of the model
     * @return List of workflows using the model
     */
    @Query("SELECT w FROM AiWorkflow w JOIN w.models m WHERE m.id = :modelId")
    List<AiWorkflow> findByModelId(@Param("modelId") UUID modelId);

    List<AiWorkflow> findScheduledWorkflows(LocalDateTime now);
}

