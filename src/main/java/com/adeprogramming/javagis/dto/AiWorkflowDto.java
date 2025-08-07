package com.adeprogramming.javagis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object for AiWorkflow entity.
 * Used for transferring AI workflow data between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiWorkflowDto {

    private UUID id;
    private String name;
    private String description;
    private String type;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime lastRunDate;
    private LocalDateTime nextScheduledRun;
    private String scheduleCron;
    private Boolean isActive;
    private Geometry areaOfInterest;
    private LocalDateTime timeRangeStart;
    private LocalDateTime timeRangeEnd;
    private List<WorkflowStepDto> steps = new ArrayList<>();
    private Map<String, String> parameters = new HashMap<>();
    private List<UUID> modelIds = new ArrayList<>();
    private List<String> modelNames = new ArrayList<>();

    /**
     * DTO for WorkflowStep embedded class.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowStepDto {
        private String name;
        private String description;
        private String type;
        private String configuration;
        private String status;
        private Integer retryCount;
        private Integer maxRetries;
    }
}

