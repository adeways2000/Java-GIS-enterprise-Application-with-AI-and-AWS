package com.adeprogramming.javagis.domain.stac;

import com.adeprogramming.javagis.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a STAC (Spatio-Temporal Asset Catalog) Link.
 * Links connect STAC resources to related resources, both internal and external.
 * This implementation follows the STAC specification tailored for BASF's environmental monitoring use cases.
 */
@Entity
@Table(name = "stac_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StacLink extends BaseEntity {

    @Column(nullable = false)
    private String rel;

    @Column(name = "link_type")
    private String type;

    @Column(name = "href", nullable = false)
    private String href;

    @Column
    private String title;

    @Column(name = "media_type")
    private String mediaType;
}

