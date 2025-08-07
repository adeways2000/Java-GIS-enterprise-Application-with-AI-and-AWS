package com.adeprogramming.javagis.domain.geospatial;
import com.adeprogramming.javagis.domain.base.BaseEntity;
import com.adeprogramming.javagis.domain.stac.StacCollection;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Entity representing a shapefile in the system.
 * Includes metadata and spatial information about the shapefile.
 */
@Entity
@Table(name = "shapefiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shapefile extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(nullable = false)
    private String source;

    @Column(name = "feature_count")
    private Integer featureCount;

    @Column(name = "attribute_schema", length = 2000)
    private String attributeSchema;

    @Column(name = "coordinate_system")
    private String coordinateSystem;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "preview_path")
    private String previewPath;

    @Column(columnDefinition = "geometry(MultiPolygon, 4326)")
    private MultiPolygon boundary;

    @Column(name = "file_size_kb")
    private Double fileSizeKb;

    @Column(name = "is_validated")
    private Boolean isValidated = false;

    @Column(name = "validation_message")
    private String validationMessage;

    @ElementCollection
    @CollectionTable(name = "shapefile_tags", joinColumns = @JoinColumn(name = "shapefile_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "shapefile_metadata", joinColumns = @JoinColumn(name = "shapefile_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> additionalMetadata = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private StacCollection collection;

    public void setUploadDate(LocalDateTime now) {
    }

    public void setFileFormat(String shapefile) {
    }

    public void setFileSizeMb(double v) {
    }

    public void setGeometry(Geometry geometry) {
    }

    public void setIsProcessed(boolean b) {
    }

    public void setProcessingLevel(String processed) {
    }

    public LocalDateTime getUploadDate() {
        return null;
    }

    public String getFileFormat() {
        return null;
    }

    public Double getFileSizeMb() {
        return null;
    }

    public Geometry getGeometry() {
        return null;
    }

    public Boolean getIsProcessed() {
        return null;
    }

    public String getProcessingLevel() {
        return null;
    }
}
