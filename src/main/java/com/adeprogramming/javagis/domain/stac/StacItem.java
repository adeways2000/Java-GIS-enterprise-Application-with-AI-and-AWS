package com.adeprogramming.javagis.domain.stac;

import com.adeprogramming.javagis.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity representing a STAC (Spatio-Temporal Asset Catalog) Item.
 * Items are the core granular entities in a STAC catalog that contain metadata about a spatio-temporal asset.
 * This implementation follows the STAC specification tailored for BASF's environmental monitoring use cases.
 */
@Entity
@Table(name = "stac_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StacItem extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String stacId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "datetime", nullable = false)
    private LocalDateTime datetime;

    @Column(name = "start_datetime")
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime")
    private LocalDateTime endDatetime;

    @Column(columnDefinition = "geometry(Geometry, 4326)")
    private Geometry geometry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private StacCollection collection;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "item_id")
    private List<StacAsset> assets = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "item_id")
    private List<StacLink> links = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "stac_item_properties", joinColumns = @JoinColumn(name = "item_id"))
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value", length = 1000)
    private Map<String, String> properties = new HashMap<>();

    /**
     * Add an asset to this item.
     *
     * @param asset The asset to add
     */
    public void addAsset(StacAsset asset) {
        assets.add(asset);
    }

    /**
     * Remove an asset from this item.
     *
     * @param asset The asset to remove
     */
    public void removeAsset(StacAsset asset) {
        assets.remove(asset);
    }

    /**
     * Add a link to this item.
     *
     * @param link The link to add
     */
    public void addLink(StacLink link) {
        links.add(link);
    }

    /**
     * Remove a link from this item.
     *
     * @param link The link to remove
     */
    public void removeLink(StacLink link) {
        links.remove(link);
    }
}
