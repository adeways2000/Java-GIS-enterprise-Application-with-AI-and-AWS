package com.adeprogramming.javagis.repository;

import com.adeprogramming.javagis.domain.stac.StacItem;
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
 * Repository for managing StacItem entities.
 * Provides methods for spatial and temporal queries on STAC items.
 */
@Repository
public interface StacItemRepository extends JpaRepository<StacItem, UUID> {

    /**
     * Find an item by its STAC ID.
     *
     * @param stacId The STAC ID
     * @return The item, if found
     */
    Optional<StacItem> findByStacId(String stacId);

    /**
     * Find items by title containing the given text (case-insensitive).
     *
     * @param title The title to search for
     * @return List of matching items
     */
    List<StacItem> findByTitleContainingIgnoreCase(String title);

    /**
     * Find items by collection ID.
     *
     * @param collectionId The collection ID
     * @return List of matching items
     */
    List<StacItem> findByCollectionId(UUID collectionId);

    /**
     * Find items by collection STAC ID.
     *
     * @param collectionStacId The collection STAC ID
     * @return List of matching items
     */
    @Query("SELECT i FROM StacItem i WHERE i.collection.stacId = :collectionStacId")
    List<StacItem> findByCollectionStacId(@Param("collectionStacId") String collectionStacId);

    /**
     * Find items with datetime between the given dates.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching items
     */
    List<StacItem> findByDatetimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find items with temporal extent overlapping the given time range.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching items
     */
    @Query("SELECT i FROM StacItem i WHERE " +
            "(i.startDatetime IS NULL OR i.startDatetime <= :endDate) AND " +
            "(i.endDatetime IS NULL OR i.endDatetime >= :startDate)")
    List<StacItem> findByTemporalExtentOverlapping(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find items that intersect with the given geometry.
     *
     * @param geometry The geometry to check for intersection
     * @return List of matching items
     */
    @Query(value = "SELECT i FROM StacItem i WHERE ST_Intersects(i.geometry, :geometry) = true")
    List<StacItem> findByIntersectingGeometry(@Param("geometry") Geometry geometry);

    /**
     * Find items by property value.
     *
     * @param propertyKey The property key
     * @param propertyValue The property value
     * @return List of matching items
     */
    @Query("SELECT i FROM StacItem i JOIN i.properties p " +
            "WHERE KEY(p) = :propertyKey AND VALUE(p) = :propertyValue")
    List<StacItem> findByPropertyValue(
            @Param("propertyKey") String propertyKey,
            @Param("propertyValue") String propertyValue);

    /**
     * Find items by asset type.
     *
     * @param assetType The asset type
     * @return List of matching items
     */
    @Query("SELECT i FROM StacItem i JOIN i.assets a WHERE a.type = :assetType")
    List<StacItem> findByAssetType(@Param("assetType") String assetType);

    long countByCollectionId(UUID id);
}

