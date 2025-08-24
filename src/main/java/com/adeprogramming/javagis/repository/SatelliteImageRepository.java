package com.adeprogramming.javagis.repository;

import com.adeprogramming.javagis.domain.geospatial.SatelliteImage;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing SatelliteImage entities.
 * Provides methods for spatial and temporal queries on satellite imagery.
 */
@Repository
public interface SatelliteImageRepository extends JpaRepository<SatelliteImage, UUID> {

    /**
     * Find satellite images by name containing the given text (case-insensitive).
     *
     * @param name The name to search for
     * @return List of matching satellite images
     */
    List<SatelliteImage> findByNameContainingIgnoreCase(String name);

    /**
     * Find satellite images by source.
     *
     * @param source The source of the satellite images
     * @return List of matching satellite images
     */
    List<SatelliteImage> findBySource(String source);

    /**
     * Find satellite images acquired between the given dates.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching satellite images
     */
    List<SatelliteImage> findByAcquisitionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find satellite images with cloud cover percentage less than the given value.
     *
     * @param cloudCoverPercentage The maximum cloud cover percentage
     * @return List of matching satellite images
     */
    List<SatelliteImage> findByCloudCoverPercentageLessThan(Double cloudCoverPercentage);

    /**
     * Find satellite images with cloud cover percentage between the given range.
     *
     * @param minCloudCover The minimum cloud cover percentage
     * @param maxCloudCover The maximum cloud cover percentage
     * @return List of matching satellite images
     */
    List<SatelliteImage> findByCloudCoverPercentageBetween(Double minCloudCover, Double maxCloudCover);

    /**
     * Find satellite images with cloud cover percentage within the given range.
     * This method handles null values for open-ended ranges.
     *
     * @param minCloudCover The minimum cloud cover percentage (can be null)
     * @param maxCloudCover The maximum cloud cover percentage (can be null)
     * @return List of matching satellite images
     */
    @Query("SELECT s FROM SatelliteImage s WHERE " +
            "(:minCloudCover IS NULL OR s.cloudCoverPercentage >= :minCloudCover) AND " +
            "(:maxCloudCover IS NULL OR s.cloudCoverPercentage <= :maxCloudCover)")
    List<SatelliteImage> findByCloudCoverageRange(@Param("minCloudCover") Double minCloudCover,
                                                  @Param("maxCloudCover") Double maxCloudCover);

    /**
     * Find satellite images that intersect with the given geometry.
     *
     * @param geometry The geometry to check for intersection
     * @return List of matching satellite images
     */
    @Query(value = "SELECT s FROM SatelliteImage s WHERE ST_Intersects(s.footprint, :geometry) = true")
    List<SatelliteImage> findByIntersectingGeometry(@Param("geometry") Geometry geometry);

    /**
     * Find satellite images within the given geometry.
     *
     * @param geometry The geometry to check for containment
     * @return List of matching satellite images
     */
    @Query(value = "SELECT s FROM SatelliteImage s WHERE ST_Within(s.footprint, :geometry) = true")
    List<SatelliteImage> findByWithinGeometry(@Param("geometry") Geometry geometry);

    /**
     * Find satellite images within the given bounding box.
     *
     * @param minX Minimum X coordinate
     * @param minY Minimum Y coordinate
     * @param maxX Maximum X coordinate
     * @param maxY Maximum Y coordinate
     * @return List of matching satellite images
     */
    @Query(value = "SELECT s FROM SatelliteImage s WHERE " +
            "ST_Intersects(s.footprint, ST_MakeEnvelope(:minX, :minY, :maxX, :maxY, 4326)) = true",
            nativeQuery = false)
    List<SatelliteImage> findByBoundingBox(@Param("minX") double minX,
                                           @Param("minY") double minY,
                                           @Param("maxX") double maxX,
                                           @Param("maxY") double maxY);

    /**
     * Find satellite images within the given bounding box using native SQL.
     * Alternative implementation using native PostGIS functions.
     *
     * @param minX Minimum X coordinate
     * @param minY Minimum Y coordinate
     * @param maxX Maximum X coordinate
     * @param maxY Maximum Y coordinate
     * @return List of matching satellite images
     */
    @Query(value = "SELECT * FROM satellite_images s WHERE " +
            "ST_Intersects(s.footprint, ST_MakeEnvelope(?1, ?2, ?3, ?4, 4326))",
            nativeQuery = true)
    List<SatelliteImage> findByBoundingBoxNative(double minX, double minY, double maxX, double maxY);

    /**
     * Find satellite images by processing status.
     *
     * @param isProcessed The processing status
     * @return List of matching satellite images
     */
    List<SatelliteImage> findByIsProcessed(Boolean isProcessed);

    /**
     * Find satellite images by collection ID.
     *
     * @param collectionId The ID of the collection
     * @return List of matching satellite images
     */
    @Query("SELECT s FROM SatelliteImage s WHERE s.collection.id = :collectionId")
    List<SatelliteImage> findByCollectionId(@Param("collectionId") UUID collectionId);

    /**
     * Find satellite images by tag.
     *
     * @param tag The tag to search for
     * @return List of matching satellite images
     */
    @Query("SELECT s FROM SatelliteImage s JOIN s.tags t WHERE t = :tag")
    List<SatelliteImage> findByTag(@Param("tag") String tag);

    /**
     * Find satellite images by processing level.
     *
     * @param processingLevel The processing level
     * @return List of matching satellite images
     */
    List<SatelliteImage> findByProcessingLevel(String processingLevel);

    /**
     * Find satellite images by file format.
     *
     * @param fileFormat The file format
     * @return List of matching satellite images
     */
    List<SatelliteImage> findByFileFormat(String fileFormat);

    /**
     * Find satellite images with file size greater than the given value.
     *
     * @param fileSizeMb The minimum file size in MB
     * @return List of matching satellite images
     */
    List<SatelliteImage> findByFileSizeMbGreaterThan(Double fileSizeMb);

    /**
     * Find satellite images with file size between the given range.
     *
     * @param minSizeMb The minimum file size in MB
     * @param maxSizeMb The maximum file size in MB
     * @return List of matching satellite images
     */
    List<SatelliteImage> findByFileSizeMbBetween(Double minSizeMb, Double maxSizeMb);

    /**
     * Find satellite images by spatial resolution.
     *
     * @param spatialResolution The spatial resolution
     * @return List of matching satellite images
     */
    List<SatelliteImage> findBySpatialResolution(Double spatialResolution);

    /**
     * Find satellite images with spatial resolution better than (less than) the given value.
     *
     * @param spatialResolution The maximum spatial resolution
     * @return List of matching satellite images
     */
    List<SatelliteImage> findBySpatialResolutionLessThan(Double spatialResolution);

    /**
     * Find satellite images that contain the given point.
     *
     * @param longitude The longitude of the point
     * @param latitude The latitude of the point
     * @return List of matching satellite images
     */
    @Query(value = "SELECT s FROM SatelliteImage s WHERE " +
            "ST_Contains(s.footprint, ST_Point(:longitude, :latitude)) = true",
            nativeQuery = false)
    List<SatelliteImage> findByContainingPoint(@Param("longitude") double longitude,
                                               @Param("latitude") double latitude);

    /**
     * Find satellite images that contain the given point using native SQL.
     *
     * @param longitude The longitude of the point
     * @param latitude The latitude of the point
     * @return List of matching satellite images
     */
    @Query(value = "SELECT * FROM satellite_images s WHERE " +
            "ST_Contains(s.footprint, ST_SetSRID(ST_Point(?1, ?2), 4326))",
            nativeQuery = true)
    List<SatelliteImage> findByContainingPointNative(double longitude, double latitude);

    /**
     * Find satellite images ordered by acquisition date (newest first).
     *
     * @return List of satellite images ordered by acquisition date
     */
    List<SatelliteImage> findAllByOrderByAcquisitionDateDesc();

    /**
     * Find satellite images ordered by acquisition date (oldest first).
     *
     * @return List of satellite images ordered by acquisition date
     */
    List<SatelliteImage> findAllByOrderByAcquisitionDateAsc();

    /**
     * Find satellite images ordered by cloud cover percentage (lowest first).
     *
     * @return List of satellite images ordered by cloud cover percentage
     */
    List<SatelliteImage> findAllByOrderByCloudCoverPercentageAsc();

    /**
     * Count satellite images by source.
     *
     * @param source The source of the satellite images
     * @return Count of satellite images from the specified source
     */
    long countBySource(String source);

    /**
     * Count satellite images by processing status.
     *
     * @param isProcessed The processing status
     * @return Count of satellite images with the specified processing status
     */
    long countByIsProcessed(Boolean isProcessed);

    /**
     * Count satellite images acquired in the given date range.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return Count of satellite images acquired in the date range
     */
    long countByAcquisitionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}

