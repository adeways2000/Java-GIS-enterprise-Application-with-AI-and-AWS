package com.adeprogramming.javagis.domain.geospatial;

import com.adeprogramming.javagis.domain.base.BaseEntity;
import com.adeprogramming.javagis.domain.stac.StacCollection;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Entity representing a satellite image in the system.
 * Includes metadata and spatial information about the image.
 */
@Entity
@Table(name = "satellite_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteImage extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "acquisition_date", nullable = false)
    private LocalDateTime acquisitionDate;

    @Column(name = "cloud_cover_percentage")
    private Double cloudCoverPercentage;

    @Column(nullable = false)
    private String source;

    @Column(name = "spatial_resolution")
    private Double spatialResolution;

    @Column(name = "spectral_bands")
    private String spectralBands;

    @Column(name = "file_format", nullable = false)
    private String fileFormat;

    @Column(name = "file_size_mb")
    private Double fileSizeMb;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    @Column(name = "is_processed")
    private Boolean isProcessed = false;

    @Column(name = "processing_level")
    private String processingLevel;

    @Column(name = "coordinate_system")
    private String coordinateSystem;

    @Column(columnDefinition = "geometry(Polygon, 4326)")
    private Polygon footprint;

    @ElementCollection
    @CollectionTable(name = "satellite_image_tags", joinColumns = @JoinColumn(name = "satellite_image_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "satellite_image_metadata", joinColumns = @JoinColumn(name = "satellite_image_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> additionalMetadata = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private StacCollection collection;
}
