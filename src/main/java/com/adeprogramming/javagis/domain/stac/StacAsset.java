package com.adeprogramming.javagis.domain.stac;

import com.adeprogramming.javagis.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a STAC (Spatio-Temporal Asset Catalog) Asset.
 * Assets represent the actual data files or resources referenced by a STAC Item.
 * This implementation follows the STAC specification tailored for environmental monitoring use cases.
 */
@Entity
@Table(name = "stac_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StacAsset extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "asset_type", nullable = false)
    private String type;

    @Column(name = "media_type")
    private String mediaType;

    @Column(name = "href", nullable = false)
    private String href;

    @Column(name = "role")
    private String role;

    @ElementCollection
    @CollectionTable(name = "stac_asset_properties", joinColumns = @JoinColumn(name = "asset_id"))
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value", length = 1000)
    private Map<String, String> properties = new HashMap<>();
}

