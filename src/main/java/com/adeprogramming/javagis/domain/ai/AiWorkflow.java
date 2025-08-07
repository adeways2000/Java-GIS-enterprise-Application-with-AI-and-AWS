package com.adeprogramming.javagis.domain.ai;

import com.adeprogramming.javagis.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity representing an AI workflow in the system.
 * Used for automating geospatial data processing and analysis.
 */
@Entity
@Table(name = "ai_workflows")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AiWorkflow extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStatus status = WorkflowStatus.CREATED;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "last_run_date")
    private LocalDateTime lastRunDate;

    @Column(name = "next_scheduled_run")
    private LocalDateTime nextScheduledRun;

    @Column(name = "schedule_cron")
    private String scheduleCron;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "area_of_interest", columnDefinition = "geometry")
    private Geometry areaOfInterest;

    @Column(name = "time_range_start")
    private LocalDateTime timeRangeStart;

    @Column(name = "time_range_end")
    private LocalDateTime timeRangeEnd;

    @ElementCollection
    @CollectionTable(name = "ai_workflow_steps", joinColumns = @JoinColumn(name = "workflow_id"))
    @OrderColumn(name = "step_order")
    private List<WorkflowStep> steps = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "ai_workflow_parameters", joinColumns = @JoinColumn(name = "workflow_id"))
    @MapKeyColumn(name = "param_name")
    @Column(name = "param_value")
    private Map<String, String> parameters = new HashMap<>();

    @ManyToMany
    @JoinTable(
            name = "ai_workflow_models",
            joinColumns = @JoinColumn(name = "workflow_id"),
            inverseJoinColumns = @JoinColumn(name = "model_id")
    )
    private List<AiModel> models = new ArrayList<>();

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiAnalysisResult> results = new ArrayList<>();

    /**
     * Enum representing the type of AI workflow.
     */
    public enum WorkflowType {
        ENVIRONMENTAL_MONITORING,
        ASSET_TRACKING,
        ANOMALY_DETECTION,
        CHANGE_DETECTION,
        CLASSIFICATION,
        SEGMENTATION,
        PREDICTIVE_MAINTENANCE,
        CUSTOM
    }

    /**
     * Enum representing the status of AI workflow.
     */
    public enum WorkflowStatus {
        CREATED,
        SCHEDULED,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    /**
     * Embedded class representing a step in an AI workflow.
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowStep {

        @Column(name = "step_name", nullable = false)
        private String name;

        @Column(name = "step_description")
        private String description;

        @Column(name = "step_type", nullable = false)
        private String type;

        @Column(name = "step_config", columnDefinition = "TEXT")
        private String configuration;

        @Column(name = "step_status")
        private String status;

        @Column(name = "step_retry_count")
        private Integer retryCount = 0;

        @Column(name = "step_max_retries")
        private Integer maxRetries = 3;
    }
}

