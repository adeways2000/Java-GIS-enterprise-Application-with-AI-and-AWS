package com.adeprogramming.javagis.domain.stac;

import com.adeprogramming.javagis.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity representing a STAC (Spatio-Temporal Asset Catalog) Collection.
 * Collections are used to group STAC Items that share common metadata and properties.
 * This implementation follows the STAC specification tailored for BASF's environmental monitoring use cases.
 */
@Entity
@Table(name = "stac_collections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StacCollection extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String stacId;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "license")
    private String license;

    @Column(name = "start_datetime")
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime")
    private LocalDateTime endDatetime;

    @Column(columnDefinition = "geometry(Polygon, 4326)")
    private Polygon spatialExtent;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StacItem> items = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "collection_id")
    private List<StacLink> links = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "stac_collection_keywords", joinColumns = @JoinColumn(name = "collection_id"))
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "stac_collection_providers", joinColumns = @JoinColumn(name = "collection_id"))
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "provider_name")),
            @AttributeOverride(name = "description", column = @Column(name = "provider_description")),
            @AttributeOverride(name = "roles", column = @Column(name = "provider_roles")),
            @AttributeOverride(name = "url", column = @Column(name = "provider_url"))
    })
    private List<StacProvider> providers = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "stac_collection_properties", joinColumns = @JoinColumn(name = "collection_id"))
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value", length = 1000)
    private Map<String, String> properties = new HashMap<>();

    /**
     * Add a STAC item to this collection.
     *
     * @param item The STAC item to add
     */
    public void addItem(StacItem item) {
        items.add(item);
        item.setCollection(this);
    }

    /**
     * Remove a STAC item from this collection.
     *
     * @param item The STAC item to remove
     */
    public void removeItem(StacItem item) {
        items.remove(item);
        item.setCollection(null);
    }

    /**
     * Add a link to this collection.
     *
     * @param link The link to add
     */
    public void addLink(StacLink link) {
        links.add(link);
    }

    /**
     * Remove a link from this collection.
     *
     * @param link The link to remove
     */
    public void removeLink(StacLink link) {
        links.remove(link);
    }

    public void setProperties(Map<String, Object> properties) {
    }

    /**
     * Embedded class representing a STAC provider.
     */
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StacProvider {
        private String name;
        private String description;
        private String roles;
        private String url;
    }
}

