package com.adeprogramming.javagis.repository;

import com.adeprogramming.javagis.domain.geospatial.Shapefile;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing Shapefile entities.
 * Provides methods for spatial and temporal queries on shapefiles.
 */
@Repository
public interface ShapefileRepository extends JpaRepository<Shapefile, UUID> {

    /**
     * Find shapefiles by name containing the given text (case-insensitive).
     *
     * @param name The name to search for
     * @return List of matching shapefiles
     */
    List<Shapefile> findByNameContainingIgnoreCase(String name);

    /**
     * Find shapefiles by source.
     *
     * @param source The source of the shapefiles
     * @return List of matching shapefiles
     */
    List<Shapefile> findBySource(String source);

    /**
     * Find shapefiles created between the given dates.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching shapefiles
     */
    List<Shapefile> findByCreationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find shapefiles that intersect with the given geometry.
     *
     * @param geometry The geometry to check for intersection
     * @return List of matching shapefiles
     */
    @Query(value = "SELECT s FROM Shapefile s WHERE ST_Intersects(s.boundary, :geometry) = true")
    List<Shapefile> findByIntersectingGeometry(@Param("geometry") Geometry geometry);

    /**
     * Find shapefiles within the given geometry.
     *
     * @param geometry The geometry to check for containment
     * @return List of matching shapefiles
     */
    @Query(value = "SELECT s FROM Shapefile s WHERE ST_Within(s.boundary, :geometry) = true")
    List<Shapefile> findByWithinGeometry(@Param("geometry") Geometry geometry);

    /**
     * Find shapefiles by validation status.
     *
     * @param isValidated The validation status
     * @return List of matching shapefiles
     */
    List<Shapefile> findByIsValidated(Boolean isValidated);

    /**
     * Find shapefiles by collection ID.
     *
     * @param collectionId The ID of the collection
     * @return List of matching shapefiles
     */
    @Query("SELECT s FROM Shapefile s WHERE s.collection.id = :collectionId")
    List<Shapefile> findByCollectionId(@Param("collectionId") UUID collectionId);

    /**
     * Find shapefiles by tag.
     *
     * @param tag The tag to search for
     * @return List of matching shapefiles
     */
    @Query("SELECT s FROM Shapefile s JOIN s.tags t WHERE t = :tag")
    List<Shapefile> findByTag(@Param("tag") String tag);

    /**
     * Find shapefiles with feature count greater than the given value.
     *
     * @param featureCount The minimum feature count
     * @return List of matching shapefiles
     */
    List<Shapefile> findByFeatureCountGreaterThan(Integer featureCount);

    Arrays findByUploadDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
