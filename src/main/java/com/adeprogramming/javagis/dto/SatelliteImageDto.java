package com.adeprogramming.javagis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object for SatelliteImage entity.
 * Used for transferring satellite image data between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteImageDto {

    private UUID id;
    private String name;
    private String description;
    private LocalDateTime acquisitionDate;
    private Double cloudCoverPercentage;
    private String source;
    private Double spatialResolution;
    private String spectralBands;
    private String fileFormat;
    private Double fileSizeMb;
    private String storagePath;
    private String thumbnailPath;
    private Boolean isProcessed;
    private String processingLevel;
    private String coordinateSystem;
    private Polygon footprint;
    private List<String> tags = new ArrayList<>();
    private Map<String, String> additionalMetadata = new HashMap<>();
    private UUID collectionId;
    private String collectionName;
}
