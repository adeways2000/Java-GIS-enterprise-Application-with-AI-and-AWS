package com.adeprogramming.javagis.repository;

import com.adeprogramming.javagis.domain.stac.StacCollection;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing StacCollection entities.
 * Provides methods for spatial and temporal queries on STAC collections.
 */
@Repository
public interface StacCollectionRepository extends JpaRepository<StacCollection, UUID> {

    /**
     * Find a collection by its STAC ID.
     *
     * @param stacId The STAC ID
     * @return The collection, if found
     */
    Optional<StacCollection> findByStacId(String stacId);

    /**
     * Find collections by title containing the given text (case-insensitive).
     *
     * @param title The title to search for
     * @return List of matching collections
     */
    List<StacCollection> findByTitleContainingIgnoreCase(String title);

    /**
     * Find collections by license.
     *
     * @param license The license to search for
     * @return List of matching collections
     */
    List<StacCollection> findByLicense(String license);

    /**
     * Find collections with temporal extent overlapping the given time range.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching collections
     */
    @Query("SELECT c FROM StacCollection c WHERE " +
            "(c.startDatetime IS NULL OR c.startDatetime <= :endDate) AND " +
            "(c.endDatetime IS NULL OR c.endDatetime >= :startDate)")
    List<StacCollection> findByTemporalExtentOverlapping(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find collections that intersect with the given geometry.
     *
     * @param geometry The geometry to check for intersection
     * @return List of matching collections
     */
    @Query(value = "SELECT c FROM StacCollection c WHERE ST_Intersects(c.spatialExtent, :geometry) = true")
    List<StacCollection> findByIntersectingGeometry(@Param("geometry") Geometry geometry);

    /**
     * Find collections by keyword.
     *
     * @param keyword The keyword to search for
     * @return List of matching collections
     */
    @Query("SELECT c FROM StacCollection c JOIN c.keywords k WHERE k = :keyword")
    List<StacCollection> findByKeyword(@Param("keyword") String keyword);

    /**
     * Find collections by provider name.
     *
     * @param providerName The provider name to search for
     * @return List of matching collections
     */
    @Query("SELECT c FROM StacCollection c JOIN c.providers p WHERE p.name = :providerName")
    List<StacCollection> findByProviderName(@Param("providerName") String providerName);
}

