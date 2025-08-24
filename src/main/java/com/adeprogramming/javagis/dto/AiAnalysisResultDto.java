package com.adeprogramming.javagis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object for AiAnalysisResult entity.
 * Used for transferring AI analysis result data between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResultDto {

    private UUID id;
    private String name;
    private String description;
    private UUID workflowId;
    private String workflowName;
    private LocalDateTime executionDate;
    private LocalDateTime completionDate;
    private String status;
    private String resultPath;
    private Geometry areaOfInterest;
    private LocalDateTime timeRangeStart;
    private LocalDateTime timeRangeEnd;
    private Double confidenceScore;
    private Long processingTimeMs;
    private Map<String, String> metrics = new HashMap<>();
    private Map<String, String> resultData = new HashMap<>();

    public void setOutputFiles(String outputFiles) {
    }

    public void setQualityMetrics(Map<String, String> qualityMetrics) {
    }
}
