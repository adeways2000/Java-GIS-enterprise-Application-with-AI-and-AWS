package com.adeprogramming.javagis.domain.ai;

import com.adeprogramming.javagis.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing an AI analysis result in the system.
 * Used for storing the results of AI workflow executions.
 */
@Entity
@Table(name = "ai_analysis_results")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResult extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private AiWorkflow workflow;

    @Column(name = "execution_date", nullable = false)
    private LocalDateTime executionDate = LocalDateTime.now();

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultStatus status = ResultStatus.PENDING;

    @Column(name = "result_path")
    private String resultPath;

    @Column(name = "area_of_interest", columnDefinition = "geometry")
    private Geometry areaOfInterest;

    @Column(name = "time_range_start")
    private LocalDateTime timeRangeStart;

    @Column(name = "time_range_end")
    private LocalDateTime timeRangeEnd;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @ElementCollection
    @CollectionTable(name = "ai_analysis_result_metrics", joinColumns = @JoinColumn(name = "result_id"))
    @MapKeyColumn(name = "metric_name")
    @Column(name = "metric_value")
    private Map<String, String> metrics = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "ai_analysis_result_data", joinColumns = @JoinColumn(name = "result_id"))
    @MapKeyColumn(name = "data_key")
    @Column(name = "data_value", columnDefinition = "TEXT")
    private Map<String, String> resultData = new HashMap<>();

    public String getOutputFiles() {
        return null;
    }

    public Map<String, String> getQualityMetrics() {
        return null;
    }

    public void setResultData(Map<String, Object> resultData) {
    }

    /**
     * Enum representing the status of an AI analysis result.
     */
    public enum ResultStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}

