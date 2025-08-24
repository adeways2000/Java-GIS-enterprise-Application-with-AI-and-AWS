package com.adeprogramming.javagis.service;

import com.adeprogramming.javagis.domain.geospatial.Shapefile;
import com.adeprogramming.javagis.dto.ShapefileDto;
import com.adeprogramming.javagis.exception.GeospatialProcessingException;
import com.adeprogramming.javagis.exception.ResourceNotFoundException;
import com.adeprogramming.javagis.repository.ShapefileRepository;
import com.adeprogramming.javagis.service.aws.CloudWatchService;
import com.adeprogramming.javagis.service.aws.S3Service;
import com.adeprogramming.javagis.util.GeoToolsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service for managing shapefiles.
 * Provides methods for CRUD operations and processing of shapefiles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShapefileService {

    private final ShapefileRepository shapefileRepository;
    private final S3Service s3Service;
    private final GeoToolsUtil geoToolsUtil;
    private final CloudWatchService cloudWatchService;

    @Value("${javagis.storage.temp-dir}")
    private String tempDir;

    @Value("${javagis.storage.s3.shapefile-prefix}")
    private String s3ShapefilePrefix;

    /**
     * Find all shapefiles.
     *
     * @return List of all shapefiles
     */
    @Transactional(readOnly = true)
    public List<ShapefileDto> findAll() {
        return shapefileRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find a shapefile by ID.
     *
     * @param id The ID of the shapefile
     * @return The shapefile DTO
     * @throws ResourceNotFoundException if the shapefile is not found
     */
    @Transactional(readOnly = true)
    public ShapefileDto findById(UUID id) {
        return shapefileRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Shapefile not found with id: " + id));
    }

    /**
     * Find shapefiles by name containing the given text.
     *
     * @param name The name to search for
     * @return List of matching shapefiles
     */
    @Transactional(readOnly = true)
    public List<ShapefileDto> findByName(String name) {
        return shapefileRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find shapefiles by date range.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching shapefiles
     */
    @Transactional(readOnly = true)
    public List<ShapefileDto> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return (List<ShapefileDto>) shapefileRepository.findByUploadDateBetween(startDate, endDate);
    }

    private Object convertToDto(Object o) {
        return o;
    }

    private <T> Optional stream() {
        return null;
    }

    /**
     * Find shapefiles that intersect with the given geometry.
     *
     * @param geometry The geometry to check for intersection
     * @return List of matching shapefiles
     */
    @Transactional(readOnly = true)
    public List<ShapefileDto> findByIntersectingGeometry(Geometry geometry) {
        return shapefileRepository.findByIntersectingGeometry(geometry).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a new shapefile.
     *
     * @param file The shapefile zip file
     * @param dto The shapefile metadata
     * @return The created shapefile DTO
     * @throws IOException if there is an error processing the file
     */
    @Transactional
    public ShapefileDto create(MultipartFile file, ShapefileDto dto) throws IOException {
        long startTime = System.currentTimeMillis();

        try {
            // Extract and validate the shapefile
            String extractedPath = extractShapefile(file);

            // Find the .shp file
            Path shpFilePath = findShpFile(extractedPath);
            if (shpFilePath == null) {
                throw new GeospatialProcessingException("No .shp file found in the uploaded zip",
                        "Shapefile", "create");
            }

            // Read shapefile metadata
            List<Map<String, Object>> features = geoToolsUtil.readShapefile(shpFilePath.toString());

            // Extract geometry from first feature if available
            Geometry geometry = null;
            if (!features.isEmpty() && features.get(0).containsKey("the_geom")) {
                geometry = (Geometry) features.get(0).get("the_geom");
            }

            // Upload to S3
            String s3Key = s3ShapefilePrefix + "/" + UUID.randomUUID() + "/" + file.getOriginalFilename();
            String s3Uri = s3Service.uploadFile(file, s3Key);

            // Create shapefile entity
            Shapefile shapefile = new Shapefile();
            shapefile.setName(dto.getName() != null ? dto.getName() : file.getOriginalFilename());
            shapefile.setDescription(dto.getDescription());
            shapefile.setUploadDate(LocalDateTime.now());
            shapefile.setFileFormat("Shapefile");
            shapefile.setFileSizeMb((double) file.getSize() / (1024 * 1024));
            shapefile.setStoragePath(s3Uri);
            shapefile.setFeatureCount(features.size());
            shapefile.setGeometry(geometry);
            shapefile.setCoordinateSystem(dto.getCoordinateSystem());

            if (dto.getTags() != null) {
                shapefile.setTags((Set<String>) dto.getTags());
            }

            if (dto.getAdditionalMetadata() != null) {
                shapefile.setAdditionalMetadata(dto.getAdditionalMetadata());
            }

            // Save to database
            Shapefile savedShapefile = shapefileRepository.save(shapefile);

            // Clean up temp files
            cleanupTempFiles(extractedPath);

            // Monitor processing time
            long processingTime = System.currentTimeMillis() - startTime;
            cloudWatchService.monitorGeospatialProcessing("Create", "Shapefile", processingTime);

            return convertToDto(savedShapefile);
        } catch (Exception e) {
            log.error("Error creating shapefile: {}", e.getMessage(), e);
            throw new GeospatialProcessingException("Failed to create shapefile: " + e.getMessage(),
                    "Shapefile", "create", e);
        }
    }

    /**
     * Update an existing shapefile.
     *
     * @param id The ID of the shapefile to update
     * @param dto The updated shapefile metadata
     * @return The updated shapefile DTO
     * @throws ResourceNotFoundException if the shapefile is not found
     */
    @Transactional
    public ShapefileDto update(UUID id, ShapefileDto dto) {
        Shapefile shapefile = shapefileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shapefile not found with id: " + id));

        // Update fields
        if (dto.getName() != null) {
            shapefile.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            shapefile.setDescription(dto.getDescription());
        }

        if (dto.getCoordinateSystem() != null) {
            shapefile.setCoordinateSystem(dto.getCoordinateSystem());
        }

        if (dto.getTags() != null) {
            shapefile.setTags((Set<String>) dto.getTags());
        }

        if (dto.getAdditionalMetadata() != null) {
            shapefile.setAdditionalMetadata(dto.getAdditionalMetadata());
        }

        // Save the updated entity
        Shapefile updatedShapefile = shapefileRepository.save(shapefile);

        return convertToDto(updatedShapefile);
    }

    /**
     * Delete a shapefile.
     *
     * @param id The ID of the shapefile to delete
     * @throws ResourceNotFoundException if the shapefile is not found
     */
    @Transactional
    public void delete(UUID id) {
        Shapefile shapefile = shapefileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shapefile not found with id: " + id));

        // Delete from S3 if stored there
        if (shapefile.getStoragePath() != null && shapefile.getStoragePath().startsWith("s3://")) {
            String s3Key = shapefile.getStoragePath().substring(5); // Remove "s3://"
            s3Key = s3Key.substring(s3Key.indexOf('/') + 1); // Remove bucket name
            s3Service.deleteFile(s3Key);
        }

        // Delete from database
        shapefileRepository.delete(shapefile);
    }

    /**
     * Process a shapefile.
     *
     * @param id The ID of the shapefile to process
     * @return The processed shapefile DTO
     * @throws ResourceNotFoundException if the shapefile is not found
     */
    @Transactional
    public ShapefileDto processShapefile(UUID id) {
        long startTime = System.currentTimeMillis();

        try {
            Shapefile shapefile = shapefileRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Shapefile not found with id: " + id));

            // Download from S3 if needed
            // Process shapefile (e.g., reprojection, simplification, etc.)
            // Update metadata

            // For now, just mark as processed
            shapefile.setIsProcessed(true);
            shapefile.setProcessingLevel("Processed");

            // Save the updated entity
            Shapefile processedShapefile = shapefileRepository.save(shapefile);

            // Monitor processing time
            long processingTime = System.currentTimeMillis() - startTime;
            cloudWatchService.monitorGeospatialProcessing("Process", "Shapefile", processingTime);

            return convertToDto(processedShapefile);
        } catch (Exception e) {
            log.error("Error processing shapefile: {}", e.getMessage(), e);
            throw new GeospatialProcessingException("Failed to process shapefile: " + e.getMessage(),
                    "Shapefile", "process", e);
        }
    }

    /**
     * Extract a shapefile from a zip file.
     *
     * @param file The zip file containing the shapefile
     * @return The path to the extracted directory
     * @throws IOException if there is an error extracting the file
     */
    private String extractShapefile(MultipartFile file) throws IOException {
        // Create temp directory
        File tempDirFile = new File(tempDir);
        if (!tempDirFile.exists()) {
            tempDirFile.mkdirs();
        }

        // Create directory for this extraction
        String extractDir = tempDir + "/" + UUID.randomUUID();
        File extractDirFile = new File(extractDir);
        extractDirFile.mkdirs();

        // Extract zip file
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];

            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(extractDir, entry.getName());

                // Create directories if needed
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                    continue;
                }

                // Create parent directories if needed
                new File(newFile.getParent()).mkdirs();

                // Write file content
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }

        return extractDir;
    }

    /**
     * Find the .shp file in the extracted directory.
     *
     * @param extractedPath The path to the extracted directory
     * @return The path to the .shp file, or null if not found
     * @throws IOException if there is an error reading the directory
     */
    private Path findShpFile(String extractedPath) throws IOException {
        return Files.walk(Path.of(extractedPath))
                .filter(path -> path.toString().toLowerCase().endsWith(".shp"))
                .findFirst()
                .orElse(null);
    }

    /**
     * Clean up temporary files.
     *
     * @param extractedPath The path to the extracted directory
     */
    private void cleanupTempFiles(String extractedPath) {
        try {
            Files.walk(Path.of(extractedPath))
                    .sorted((a, b) -> b.toString().compareTo(a.toString())) // Reverse order to delete files before directories
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete temp file: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.warn("Failed to clean up temp files: {}", e.getMessage(), e);
        }
    }

    /**
     * Convert a Shapefile entity to a DTO.
     *
     * @param shapefile The entity to convert
     * @return The DTO
     */
    private ShapefileDto convertToDto(Shapefile shapefile) {
        ShapefileDto dto = new ShapefileDto();
        dto.setId(shapefile.getId());
        dto.setName(shapefile.getName());
        dto.setDescription(shapefile.getDescription());
        dto.setUploadDate(shapefile.getUploadDate());
        dto.setFileFormat(shapefile.getFileFormat());
        dto.setFileSizeMb(shapefile.getFileSizeMb());
        dto.setStoragePath(shapefile.getStoragePath());
        dto.setFeatureCount(shapefile.getFeatureCount());
        dto.setGeometry(shapefile.getGeometry());
        dto.setCoordinateSystem(shapefile.getCoordinateSystem());
        dto.setIsProcessed(shapefile.getIsProcessed());
        dto.setProcessingLevel(shapefile.getProcessingLevel());

        if (shapefile.getTags() != null) {
            dto.setTags((List<String>) shapefile.getTags());
        }

        if (shapefile.getAdditionalMetadata() != null) {
            dto.setAdditionalMetadata(shapefile.getAdditionalMetadata());
        }

        return dto;
    }
}
