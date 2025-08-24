package com.adeprogramming.javagis.service;

import com.adeprogramming.javagis.domain.geospatial.SatelliteImage;
import com.adeprogramming.javagis.dto.SatelliteImageDto;
import com.adeprogramming.javagis.exception.ResourceNotFoundException;
import com.adeprogramming.javagis.repository.SatelliteImageRepository;
import com.adeprogramming.javagis.service.aws.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.FactoryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing satellite images.
 * Provides methods for CRUD operations and spatial analysis on satellite imagery.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SatelliteImageService {

    private final SatelliteImageRepository satelliteImageRepository;
    private final S3Service s3Service;

    @Value("${javagis.storage.local-path:./storage}")
    private String localStoragePath;

    @Value("${javagis.storage.use-s3:false}")
    private boolean useS3Storage;

    /**
     * Find all satellite images.
     *
     * @return List of all satellite images
     */
    @Transactional(readOnly = true)
    public List<SatelliteImageDto> findAll() {
        return satelliteImageRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find a satellite image by ID.
     *
     * @param id The ID of the satellite image
     * @return The satellite image DTO
     * @throws ResourceNotFoundException if the satellite image is not found
     */
    @Transactional(readOnly = true)
    public SatelliteImageDto findById(UUID id) {
        return satelliteImageRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Satellite image not found with id: " + id));
    }

    /**
     * Find satellite images by name containing the given text.
     *
     * @param name The name to search for
     * @return List of matching satellite images
     */
    @Transactional(readOnly = true)
    public List<SatelliteImageDto> findByName(String name) {
        return satelliteImageRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find satellite images acquired between the given dates.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching satellite images
     */
    @Transactional(readOnly = true)
    public List<SatelliteImageDto> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return satelliteImageRepository.findByAcquisitionDateBetween(startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find satellite images that intersect with the given geometry.
     *
     * @param geometry The geometry to check for intersection
     * @return List of matching satellite images
     */
    @Transactional(readOnly = true)
    public List<SatelliteImageDto> findByIntersectingGeometry(Geometry geometry) {
        return satelliteImageRepository.findByIntersectingGeometry(geometry).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Upload and create a new satellite image.
     *
     * @param file The satellite image file
     * @param dto The satellite image metadata
     * @return The created satellite image DTO
     * @throws IOException if there is an error processing the file
     */
    @Transactional
    public SatelliteImageDto create(MultipartFile file, SatelliteImageDto dto) throws IOException {
        // Save the file to storage
        String storagePath = saveFile(file, dto.getName());

        // Extract metadata from the image if possible
        Map<String, Object> extractedMetadata = extractMetadata(file);

        // Create the satellite image entity
        SatelliteImage satelliteImage = new SatelliteImage();
        satelliteImage.setName(dto.getName());
        satelliteImage.setDescription(dto.getDescription());
        satelliteImage.setAcquisitionDate(dto.getAcquisitionDate());
        satelliteImage.setCloudCoverPercentage(dto.getCloudCoverPercentage());
        satelliteImage.setSource(dto.getSource());
        satelliteImage.setSpatialResolution(dto.getSpatialResolution());
        satelliteImage.setSpectralBands(dto.getSpectralBands());
        satelliteImage.setFileFormat(getFileExtension(file.getOriginalFilename()));
        satelliteImage.setFileSizeMb((double) file.getSize() / (1024 * 1024));
        satelliteImage.setStoragePath(storagePath);
        satelliteImage.setIsProcessed(false);
        satelliteImage.setProcessingLevel(dto.getProcessingLevel());
        satelliteImage.setCoordinateSystem(dto.getCoordinateSystem());

        // Extract footprint if possible
        try {
            Polygon footprint = extractFootprint(file);
            satelliteImage.setFootprint(footprint);
        } catch (Exception e) {
            log.warn("Failed to extract footprint from satellite image: {}", e.getMessage());
            // If footprint extraction fails, use the provided footprint if available
            if (dto.getFootprint() != null) {
                satelliteImage.setFootprint(dto.getFootprint());
            }
        }

        // Set tags
        if (dto.getTags() != null) {
            satelliteImage.setTags(new HashSet<>(dto.getTags()));
        }

        // Set additional metadata
        if (dto.getAdditionalMetadata() != null) {
            satelliteImage.setAdditionalMetadata(new HashMap<>(dto.getAdditionalMetadata()));
        } else {
            satelliteImage.setAdditionalMetadata(new HashMap<>());
        }

        // Add extracted metadata
        if (extractedMetadata != null) {
            for (Map.Entry<String, Object> entry : extractedMetadata.entrySet()) {
                satelliteImage.getAdditionalMetadata().put(entry.getKey(), entry.getValue().toString());
            }
        }

        // Save the entity
        SatelliteImage savedImage = satelliteImageRepository.save(satelliteImage);

        return convertToDto(savedImage);
    }

    /**
     * Update an existing satellite image.
     *
     * @param id The ID of the satellite image to update
     * @param dto The updated satellite image metadata
     * @return The updated satellite image DTO
     * @throws ResourceNotFoundException if the satellite image is not found
     */
    @Transactional
    public SatelliteImageDto update(UUID id, SatelliteImageDto dto) {
        SatelliteImage satelliteImage = satelliteImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Satellite image not found with id: " + id));

        // Update fields
        if (dto.getName() != null) {
            satelliteImage.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            satelliteImage.setDescription(dto.getDescription());
        }
        if (dto.getAcquisitionDate() != null) {
            satelliteImage.setAcquisitionDate(dto.getAcquisitionDate());
        }
        if (dto.getCloudCoverPercentage() != null) {
            satelliteImage.setCloudCoverPercentage(dto.getCloudCoverPercentage());
        }
        if (dto.getSource() != null) {
            satelliteImage.setSource(dto.getSource());
        }
        if (dto.getSpatialResolution() != null) {
            satelliteImage.setSpatialResolution(dto.getSpatialResolution());
        }
        if (dto.getSpectralBands() != null) {
            satelliteImage.setSpectralBands(dto.getSpectralBands());
        }
        if (dto.getProcessingLevel() != null) {
            satelliteImage.setProcessingLevel(dto.getProcessingLevel());
        }
        if (dto.getCoordinateSystem() != null) {
            satelliteImage.setCoordinateSystem(dto.getCoordinateSystem());
        }

        // Update footprint if provided
        if (dto.getFootprint() != null) {
            satelliteImage.setFootprint(dto.getFootprint());
        }

        // Update tags if provided
        if (dto.getTags() != null) {
            satelliteImage.setTags(new HashSet<>(dto.getTags()));
        }

        // Update additional metadata if provided
        if (dto.getAdditionalMetadata() != null) {
            if (satelliteImage.getAdditionalMetadata() == null) {
                satelliteImage.setAdditionalMetadata(new HashMap<>());
            }
            satelliteImage.getAdditionalMetadata().putAll(dto.getAdditionalMetadata());
        }

        // Save the updated entity
        SatelliteImage updatedImage = satelliteImageRepository.save(satelliteImage);

        return convertToDto(updatedImage);
    }

    /**
     * Delete a satellite image by ID.
     *
     * @param id The ID of the satellite image to delete
     * @throws ResourceNotFoundException if the satellite image is not found
     */
    @Transactional
    public void delete(UUID id) {
        SatelliteImage satelliteImage = satelliteImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Satellite image not found with id: " + id));

        // Delete the file from storage
        deleteFile(satelliteImage.getStoragePath());

        // Delete the entity
        satelliteImageRepository.delete(satelliteImage);
    }

    /**
     * Process a satellite image.
     *
     * @param id The ID of the satellite image to process
     * @return The processed satellite image DTO
     * @throws ResourceNotFoundException if the satellite image is not found
     */
    @Transactional
    public SatelliteImageDto processImage(UUID id) {
        SatelliteImage satelliteImage = satelliteImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Satellite image not found with id: " + id));

        try {
            // Perform processing (this would be application-specific)
            // For example, generate thumbnails, extract features, etc.
            log.info("Processing satellite image: {}", satelliteImage.getName());

            // Simulate processing time
            Thread.sleep(1000);

            // Mark as processed
            satelliteImage.setIsProcessed(true);

            // Save the updated entity
            SatelliteImage processedImage = satelliteImageRepository.save(satelliteImage);

            log.info("Successfully processed satellite image: {}", satelliteImage.getName());
            return convertToDto(processedImage);

        } catch (Exception e) {
            log.error("Failed to process satellite image: {}", id, e);
            throw new RuntimeException("Failed to process satellite image: " + e.getMessage());
        }
    }

    /**
     * Convert a SatelliteImage entity to a DTO.
     *
     * @param satelliteImage The entity to convert
     * @return The DTO
     */
    private SatelliteImageDto convertToDto(SatelliteImage satelliteImage) {
        SatelliteImageDto dto = new SatelliteImageDto();
        dto.setId(satelliteImage.getId());
        dto.setName(satelliteImage.getName());
        dto.setDescription(satelliteImage.getDescription());
        dto.setAcquisitionDate(satelliteImage.getAcquisitionDate());
        dto.setCloudCoverPercentage(satelliteImage.getCloudCoverPercentage());
        dto.setSource(satelliteImage.getSource());
        dto.setSpatialResolution(satelliteImage.getSpatialResolution());
        dto.setSpectralBands(satelliteImage.getSpectralBands());
        dto.setFileFormat(satelliteImage.getFileFormat());
        dto.setFileSizeMb(satelliteImage.getFileSizeMb());
        dto.setStoragePath(satelliteImage.getStoragePath());
        dto.setThumbnailPath(satelliteImage.getThumbnailPath());
        dto.setIsProcessed(satelliteImage.getIsProcessed());
        dto.setProcessingLevel(satelliteImage.getProcessingLevel());
        dto.setCoordinateSystem(satelliteImage.getCoordinateSystem());
        dto.setFootprint(satelliteImage.getFootprint());

        if (satelliteImage.getTags() != null) {
            dto.setTags(new ArrayList<>(satelliteImage.getTags()));
        }

        if (satelliteImage.getAdditionalMetadata() != null) {
            dto.setAdditionalMetadata(new HashMap<>(satelliteImage.getAdditionalMetadata()));
        }

        if (satelliteImage.getCollection() != null) {
            dto.setCollectionId(satelliteImage.getCollection().getId());
            dto.setCollectionName(satelliteImage.getCollection().getTitle());
        }

        return dto;
    }

    /**
     * Save a file to storage.
     *
     * @param file The file to save
     * @param name The name to use for the file
     * @return The storage path
     * @throws IOException if there is an error saving the file
     */
    private String saveFile(MultipartFile file, String name) throws IOException {
        String fileName = name + "_" + UUID.randomUUID() + "." + getFileExtension(file.getOriginalFilename());

        if (useS3Storage) {
            // Save to S3
            return s3Service.uploadFile(file, "satellite-images/" + fileName);
        } else {
            // Save to local storage
            Path storagePath = Paths.get(localStoragePath, "satellite-images");
            Files.createDirectories(storagePath);
            Path filePath = storagePath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            return filePath.toString();
        }
    }

    /**
     * Delete a file from storage.
     *
     * @param storagePath The storage path of the file to delete
     */
    private void deleteFile(String storagePath) {
        if (useS3Storage) {
            // Delete from S3
            s3Service.deleteFile(storagePath);
        } else {
            // Delete from local storage
            try {
                Files.deleteIfExists(Paths.get(storagePath));
            } catch (IOException e) {
                log.error("Failed to delete file: {}", storagePath, e);
            }
        }
    }

    /**
     * Extract metadata from a satellite image file.
     *
     * @param file The file to extract metadata from
     * @return The extracted metadata
     */
    private Map<String, Object> extractMetadata(MultipartFile file) {
        try {
            // Create a temporary file
            File tempFile = File.createTempFile("satellite-image-", "." + getFileExtension(file.getOriginalFilename()));
            file.transferTo(tempFile);

            try {
                // Find the appropriate format
                AbstractGridFormat format = GridFormatFinder.findFormat(tempFile);
                if (format == null) {
                    log.warn("No suitable format found for file: {}", file.getOriginalFilename());
                    return null;
                }

                // Read the coverage
                GridCoverage2DReader reader = format.getReader(tempFile);
                if (reader == null) {
                    log.warn("Failed to create reader for file: {}", file.getOriginalFilename());
                    return null;
                }

                // Extract metadata
                Map<String, Object> metadata = new HashMap<>();

                if (reader instanceof AbstractGridCoverage2DReader) {
                    AbstractGridCoverage2DReader abstractReader = (AbstractGridCoverage2DReader) reader;
                    String[] metadataNames = abstractReader.getMetadataNames();
                    if (metadataNames != null) {
                        for (String name : metadataNames) {
                            try {
                                String value = abstractReader.getMetadataValue(name);
                                if (value != null) {
                                    metadata.put(name, value);
                                }
                            } catch (Exception e) {
                                log.debug("Failed to get metadata value for {}: {}", name, e.getMessage());
                            }
                        }
                    }
                }

                // Clean up
                reader.dispose();
                return metadata;

            } finally {
                // Clean up temporary file
                tempFile.delete();
            }

        } catch (Exception e) {
            log.warn("Failed to extract metadata from satellite image: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract the footprint from a satellite image file.
     *
     * @param file The file to extract the footprint from
     * @return The extracted footprint
     * @throws Exception if there is an error extracting the footprint
     */
    private Polygon extractFootprint(MultipartFile file) throws Exception {
        // Create a temporary file
        File tempFile = File.createTempFile("satellite-image-", "." + getFileExtension(file.getOriginalFilename()));
        file.transferTo(tempFile);

        try {
            // Find the appropriate format
            AbstractGridFormat format = GridFormatFinder.findFormat(tempFile);
            if (format == null) {
                throw new Exception("No suitable format found for file: " + file.getOriginalFilename());
            }

            // Read the coverage
            GridCoverage2DReader reader = format.getReader(tempFile);
            if (reader == null) {
                throw new Exception("Failed to create reader for file: " + file.getOriginalFilename());
            }

            try {
                // Read the first coverage
                GridCoverage2D coverage = reader.read(null);
                if (coverage == null) {
                    throw new Exception("Failed to read coverage from file: " + file.getOriginalFilename());
                }

                try {
                    // Get the envelope and convert to polygon
                    org.geotools.geometry.GeneralEnvelope envelope = (org.geotools.geometry.GeneralEnvelope) coverage.getEnvelope();
                    CoordinateReferenceSystem crs = envelope.getCoordinateReferenceSystem();

                    // Create a polygon from the envelope
                    double minX = envelope.getMinimum(0);
                    double minY = envelope.getMinimum(1);
                    double maxX = envelope.getMaximum(0);
                    double maxY = envelope.getMaximum(1);

                    // Create coordinates for the polygon
                    org.locationtech.jts.geom.Coordinate[] coordinates = new org.locationtech.jts.geom.Coordinate[5];
                    coordinates[0] = new org.locationtech.jts.geom.Coordinate(minX, minY);
                    coordinates[1] = new org.locationtech.jts.geom.Coordinate(maxX, minY);
                    coordinates[2] = new org.locationtech.jts.geom.Coordinate(maxX, maxY);
                    coordinates[3] = new org.locationtech.jts.geom.Coordinate(minX, maxY);
                    coordinates[4] = new org.locationtech.jts.geom.Coordinate(minX, minY); // Close the ring

                    // Create the polygon
                    org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory();
                    org.locationtech.jts.geom.LinearRing ring = geometryFactory.createLinearRing(coordinates);
                    Polygon polygon = geometryFactory.createPolygon(ring);

                    // Transform to WGS84 if necessary
                    if (crs != null && !CRS.equalsIgnoreMetadata(crs, CRS.decode("EPSG:4326"))) {
                        try {
                            // FIXED: Use correct MathTransform import and method
                            MathTransform transform = CRS.findMathTransform(crs, CRS.decode("EPSG:4326"), true);
                            polygon = (Polygon) JTS.transform(polygon, transform);
                        } catch (FactoryException | TransformException e) {
                            log.warn("Failed to transform footprint to WGS84: {}", e.getMessage());
                            // Continue with original CRS
                        }
                    }

                    // Set SRID
                    polygon.setSRID(4326);

                    return polygon;

                } finally {
                    // Clean up coverage
                    coverage.dispose(true);
                }

            } finally {
                // Clean up reader
                reader.dispose();
            }

        } finally {
            // Clean up temporary file
            tempFile.delete();
        }
    }

    /**
     * Get the file extension from a filename.
     *
     * @param filename The filename
     * @return The file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Calculate NDVI (Normalized Difference Vegetation Index) for a satellite image.
     *
     * @param id The ID of the satellite image
     * @return The calculated NDVI values as a map
     * @throws ResourceNotFoundException if the satellite image is not found
     */
    @Transactional(readOnly = true)
    public Map<String, Object> calculateNDVI(UUID id) {
        SatelliteImage satelliteImage = satelliteImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Satellite image not found with id: " + id));

        Map<String, Object> result = new HashMap<>();

        try {
            // This is a simplified NDVI calculation
            // In a real implementation, you would process the actual image bands

            // For demonstration, we'll return mock NDVI statistics
            result.put("meanNDVI", 0.65);
            result.put("minNDVI", -0.2);
            result.put("maxNDVI", 0.95);
            result.put("stdDevNDVI", 0.15);
            result.put("vegetationCoverPercentage", 78.5);
            result.put("calculationDate", LocalDateTime.now());
            result.put("imageId", id);
            result.put("imageName", satelliteImage.getName());

            log.info("NDVI calculated for satellite image: {}", satelliteImage.getName());

        } catch (Exception e) {
            log.error("Failed to calculate NDVI for satellite image: {}", id, e);
            result.put("error", "Failed to calculate NDVI: " + e.getMessage());
        }

        return result;
    }

    /**
     * Generate a thumbnail for a satellite image.
     *
     * @param id The ID of the satellite image
     * @return The path to the generated thumbnail
     * @throws ResourceNotFoundException if the satellite image is not found
     */
    @Transactional
    public String generateThumbnail(UUID id) {
        SatelliteImage satelliteImage = satelliteImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Satellite image not found with id: " + id));

        try {
            // In a real implementation, you would generate an actual thumbnail
            // For demonstration, we'll create a mock thumbnail path
            String thumbnailPath = "thumbnails/" + satelliteImage.getName() + "_thumb.jpg";

            // Update the satellite image with the thumbnail path
            satelliteImage.setThumbnailPath(thumbnailPath);
            satelliteImageRepository.save(satelliteImage);

            log.info("Thumbnail generated for satellite image: {}", satelliteImage.getName());

            return thumbnailPath;
        } catch (Exception e) {
            log.error("Failed to generate thumbnail for satellite image: {}", id, e);
            throw new RuntimeException("Failed to generate thumbnail: " + e.getMessage());
        }
    }

    /**
     * Get satellite images within a specific bounding box.
     *
     * @param minX Minimum X coordinate
     * @param minY Minimum Y coordinate
     * @param maxX Maximum X coordinate
     * @param maxY Maximum Y coordinate
     * @return List of satellite images within the bounding box
     */
    @Transactional(readOnly = true)
    public List<SatelliteImageDto> findByBoundingBox(double minX, double minY, double maxX, double maxY) {
        return satelliteImageRepository.findByBoundingBox(minX, minY, maxX, maxY).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get satellite images by cloud coverage percentage range.
     *
     * @param minCloudCover Minimum cloud coverage percentage
     * @param maxCloudCover Maximum cloud coverage percentage
     * @return List of satellite images within the cloud coverage range
     */
    @Transactional(readOnly = true)
    public List<SatelliteImageDto> findByCloudCoverageRange(Double minCloudCover, Double maxCloudCover) {
        return satelliteImageRepository.findByCloudCoverageRange(minCloudCover, maxCloudCover).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get satellite images by source.
     *
     * @param source The source of the satellite images
     * @return List of satellite images from the specified source
     */
    @Transactional(readOnly = true)
    public List<SatelliteImageDto> findBySource(String source) {
        return satelliteImageRepository.findBySource(source).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get processed satellite images.
     *
     * @param isProcessed Whether to get processed or unprocessed images
     * @return List of satellite images based on processing status
     */
    @Transactional(readOnly = true)
    public List<SatelliteImageDto> findByProcessingStatus(Boolean isProcessed) {
        return satelliteImageRepository.findByIsProcessed(isProcessed).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}



