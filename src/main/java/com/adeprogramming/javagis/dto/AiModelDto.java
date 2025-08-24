package com.adeprogramming.javagis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object for AiModel entity.
 * Used for transferring AI model data between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiModelDto {

    private UUID id;
    private String name;
    private String description;
    private String type;
    private String version;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;
    private String modelPath;
    private Boolean isActive;
    private Double accuracy;
    private String trainingDataset;
    private String validationDataset;
    private Map<String, String> parameters = new HashMap<>();
    private Map<String, String> metrics = new HashMap<>();
}

