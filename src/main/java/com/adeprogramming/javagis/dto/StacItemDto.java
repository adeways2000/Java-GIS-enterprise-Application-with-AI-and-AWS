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
 * Data Transfer Object for StacItem entity.
 * Used for transferring STAC item data between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StacItemDto {

    private UUID id;
    private String stacId;
    private String title;
    private String description;
    private LocalDateTime datetime;
    private Geometry geometry;
    private Map<String, Object> properties = new HashMap<>();
    private Map<String, StacAssetDto> assets = new HashMap<>();
    private List<StacLinkDto> links = new ArrayList<>();
    private UUID collectionId;
    private String collectionTitle;

    /**
     * DTO for STAC asset.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StacAssetDto {
        private String href;
        private String title;
        private String description;
        private String type;
        private List<String> roles = new ArrayList<>();
    }

    /**
     * DTO for STAC link.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StacLinkDto {
        private String rel;
        private String href;
        private String type;
        private String title;
    }
}
