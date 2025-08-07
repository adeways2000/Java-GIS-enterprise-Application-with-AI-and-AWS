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
 * Data Transfer Object for StacCollection entity.
 * Used for transferring STAC collection data between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StacCollectionDto {

    private UUID id;
    private String stacId;
    private String title;
    private String description;
    private String license;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private Geometry spatialExtent;
    private List<String> keywords = new ArrayList<>();
    private List<StacProviderDto> providers = new ArrayList<>();
    private Map<String, Object> properties = new HashMap<>();
    private List<StacLinkDto> links = new ArrayList<>();
    private long itemCount;

    public void setProperties(Map<String, String> properties) {
    }

    /**
     * DTO for STAC provider.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StacProviderDto {
        private String name;
        private String description;
        private String roles;
        private String url;
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
