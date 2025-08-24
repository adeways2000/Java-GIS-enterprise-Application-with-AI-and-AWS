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
 * Data Transfer Object for Shapefile entity.
 * Used for transferring shapefile data between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShapefileDto {

    private UUID id;
    private String name;
    private String description;
    private LocalDateTime uploadDate;
    private String fileFormat;
    private Double fileSizeMb;
    private String storagePath;
    private Integer featureCount;
    private Geometry geometry;
    private String coordinateSystem;
    private Boolean isProcessed;
    private String processingLevel;
    private List<String> tags = new ArrayList<>();
    private Map<String, String> additionalMetadata = new HashMap<>();
}
