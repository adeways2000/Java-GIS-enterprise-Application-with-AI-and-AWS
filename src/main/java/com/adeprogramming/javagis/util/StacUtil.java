package com.adeprogramming.javagis.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for working with STAC (Spatio-Temporal Asset Catalog).
 * Provides methods for creating and validating STAC metadata.
 */
@Component
@Slf4j
public class StacUtil {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Create STAC item metadata for a satellite image.
     *
     * @param id The unique identifier for the item
     * @param title The title of the item
     * @param description The description of the item
     * @param datetime The datetime of the item
     * @param geometry The geometry of the item
     * @param properties Additional properties
     * @param assets The assets associated with the item
     * @param links The links associated with the item
     * @return The STAC item metadata as a Map
     */
    public Map<String, Object> createStacItemMetadata(
            String id,
            String title,
            String description,
            LocalDateTime datetime,
            Object geometry,
            Map<String, Object> properties,
            Map<String, Object> assets,
            List<Map<String, String>> links) {

        Map<String, Object> stacItem = new HashMap<>();
        stacItem.put("stac_version", "1.0.0");
        stacItem.put("type", "Feature");
        stacItem.put("id", id);
        stacItem.put("title", title);
        stacItem.put("description", description);
        stacItem.put("geometry", geometry);

        // Add datetime
        Map<String, Object> allProperties = new HashMap<>();
        if (properties != null) {
            allProperties.putAll(properties);
        }
        allProperties.put("datetime", datetime.format(ISO_FORMATTER));
        stacItem.put("properties", allProperties);

        // Add assets
        stacItem.put("assets", assets != null ? assets : new HashMap<>());

        // Add links
        stacItem.put("links", links != null ? links : List.of());

        return stacItem;
    }

    /**
     * Create STAC collection metadata.
     *
     * @param id The unique identifier for the collection
     * @param title The title of the collection
     * @param description The description of the collection
     * @param license The license of the collection
     * @param extent The spatial and temporal extent of the collection
     * @param keywords The keywords associated with the collection
     * @param providers The providers of the collection
     * @param links The links associated with the collection
     * @return The STAC collection metadata as a Map
     */
    public Map<String, Object> createStacCollectionMetadata(
            String id,
            String title,
            String description,
            String license,
            Map<String, Object> extent,
            List<String> keywords,
            List<Map<String, String>> providers,
            List<Map<String, String>> links) {

        Map<String, Object> stacCollection = new HashMap<>();
        stacCollection.put("stac_version", "1.0.0");
        stacCollection.put("type", "Collection");
        stacCollection.put("id", id);
        stacCollection.put("title", title);
        stacCollection.put("description", description);
        stacCollection.put("license", license);
        stacCollection.put("extent", extent);

        if (keywords != null) {
            stacCollection.put("keywords", keywords);
        }

        if (providers != null) {
            stacCollection.put("providers", providers);
        }

        stacCollection.put("links", links != null ? links : List.of());

        return stacCollection;
    }

    /**
     * Create a STAC asset.
     *
     * @param href The URL or path to the asset
     * @param title The title of the asset
     * @param description The description of the asset
     * @param type The type of the asset
     * @param roles The roles of the asset
     * @return The STAC asset metadata as a Map
     */
    public Map<String, Object> createStacAsset(
            String href,
            String title,
            String description,
            String type,
            List<String> roles) {

        Map<String, Object> asset = new HashMap<>();
        asset.put("href", href);

        if (title != null) {
            asset.put("title", title);
        }

        if (description != null) {
            asset.put("description", description);
        }

        if (type != null) {
            asset.put("type", type);
        }

        if (roles != null && !roles.isEmpty()) {
            asset.put("roles", roles);
        }

        return asset;
    }

    /**
     * Create a STAC link.
     *
     * @param href The URL or path the link points to
     * @param rel The relationship of the link
     * @param type The media type of the link
     * @param title The title of the link
     * @return The STAC link metadata as a Map
     */
    public Map<String, String> createStacLink(
            String href,
            String rel,
            String type,
            String title) {

        Map<String, String> link = new HashMap<>();
        link.put("href", href);
        link.put("rel", rel);

        if (type != null) {
            link.put("type", type);
        }

        if (title != null) {
            link.put("title", title);
        }

        return link;
    }

    /**
     * Create a spatial and temporal extent for a STAC collection.
     *
     * @param bbox The bounding box [west, south, east, north]
     * @param startDatetime The start datetime
     * @param endDatetime The end datetime
     * @return The extent metadata as a Map
     */
    public Map<String, Object> createStacExtent(
            double[] bbox,
            LocalDateTime startDatetime,
            LocalDateTime endDatetime) {

        Map<String, Object> extent = new HashMap<>();

        // Spatial extent
        Map<String, Object> spatial = new HashMap<>();
        spatial.put("bbox", List.of(bbox));
        extent.put("spatial", spatial);

        // Temporal extent
        Map<String, Object> temporal = new HashMap<>();
        String[] interval = new String[2];
        interval[0] = startDatetime != null ? startDatetime.format(ISO_FORMATTER) : null;
        interval[1] = endDatetime != null ? endDatetime.format(ISO_FORMATTER) : null;
        temporal.put("interval", List.of(interval));
        extent.put("temporal", temporal);

        return extent;
    }

    /**
     * Generate a unique STAC ID.
     *
     * @param prefix The prefix for the ID
     * @return A unique STAC ID
     */
    public String generateStacId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString();
    }

    /**
     * Validate a STAC item metadata.
     *
     * @param stacItem The STAC item metadata to validate
     * @return true if valid, false otherwise
     */
    public boolean validateStacItem(Map<String, Object> stacItem) {
        // Check required fields
        if (!stacItem.containsKey("stac_version") ||
                !stacItem.containsKey("type") ||
                !stacItem.containsKey("id") ||
                !stacItem.containsKey("geometry") ||
                !stacItem.containsKey("properties")) {
            return false;
        }

        // Check type
        if (!"Feature".equals(stacItem.get("type"))) {
            return false;
        }

        // Check properties
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) stacItem.get("properties");
        if (!properties.containsKey("datetime")) {
            return false;
        }

        return true;
    }

    /**
     * Validate a STAC collection metadata.
     *
     * @param stacCollection The STAC collection metadata to validate
     * @return true if valid, false otherwise
     */
    public boolean validateStacCollection(Map<String, Object> stacCollection) {
        // Check required fields
        if (!stacCollection.containsKey("stac_version") ||
                !stacCollection.containsKey("type") ||
                !stacCollection.containsKey("id") ||
                !stacCollection.containsKey("description") ||
                !stacCollection.containsKey("license") ||
                !stacCollection.containsKey("extent")) {
            return false;
        }

        // Check type
        if (!"Collection".equals(stacCollection.get("type"))) {
            return false;
        }

        // Check extent
        @SuppressWarnings("unchecked")
        Map<String, Object> extent = (Map<String, Object>) stacCollection.get("extent");
        if (!extent.containsKey("spatial") || !extent.containsKey("temporal")) {
            return false;
        }

        return true;
    }
}
