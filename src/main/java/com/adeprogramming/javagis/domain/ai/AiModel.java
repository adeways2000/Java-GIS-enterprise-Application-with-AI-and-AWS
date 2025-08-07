package com.adeprogramming.javagis.domain.ai;

import com.adeprogramming.javagis.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity representing an AI model in the system.
 * Used for machine learning and deep learning models for geospatial analysis.
 */
@Entity
@Table(name = "ai_models")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AiModel extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModelType type;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;

    @Column(name = "model_path")
    private String modelPath;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "accuracy")
    private Double accuracy;

    @Column(name = "training_dataset")
    private String trainingDataset;

    @Column(name = "validation_dataset")
    private String validationDataset;

    @ElementCollection
    @CollectionTable(name = "ai_model_parameters", joinColumns = @JoinColumn(name = "model_id"))
    @MapKeyColumn(name = "param_name")
    @Column(name = "param_value")
    private Map<String, String> parameters = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "ai_model_metrics", joinColumns = @JoinColumn(name = "model_id"))
    @MapKeyColumn(name = "metric_name")
    @Column(name = "metric_value")
    private Map<String, String> metrics = new HashMap<>();

    @ManyToMany(mappedBy = "models")
    private List<AiWorkflow> workflows = new ArrayList<>();

    /**
     * Enum representing the type of AI model.
     */
    public enum ModelType {
        CLASSIFICATION,
        OBJECT_DETECTION,
        SEGMENTATION,
        CHANGE_DETECTION,
        ANOMALY_DETECTION,
        REGRESSION,
        TIME_SERIES,
        CUSTOM
    }
}

