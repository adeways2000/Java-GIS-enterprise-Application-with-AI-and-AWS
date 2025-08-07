package com.adeprogramming.javagis.controller;

import com.adeprogramming.javagis.dto.SatelliteImageDto;
import com.adeprogramming.javagis.exception.GeospatialProcessingException;
import com.adeprogramming.javagis.service.SatelliteImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing satellite images.
 * Provides endpoints for CRUD operations and spatial queries on satellite imagery.
 */
@RestController
@RequestMapping("/api/satellite-images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Satellite Image API", description = "API for managing satellite images")
public class SatelliteImageController {

    private final SatelliteImageService satelliteImageService;

    /**
     * Get all satellite images.
     *
     * @return List of all satellite images
     */
    @GetMapping
    @Operation(summary = "Get all satellite images")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all satellite images",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SatelliteImageDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SatelliteImageDto>> getAllSatelliteImages() {
        List<SatelliteImageDto> images = satelliteImageService.findAll();
        return ResponseEntity.ok(images);
    }

    /**
     * Get a satellite image by ID.
     *
     * @param id The ID of the satellite image
     * @return The satellite image
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a satellite image by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the satellite image",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SatelliteImageDto.class))),
            @ApiResponse(responseCode = "404", description = "Satellite image not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SatelliteImageDto> getSatelliteImageById(
            @Parameter(description = "ID of the satellite image to retrieve") @PathVariable UUID id) {
        SatelliteImageDto image = satelliteImageService.findById(id);
        return ResponseEntity.ok(image);
    }

    /**
     * Search for satellite images by name.
     *
     * @param name The name to search for
     * @return List of matching satellite images
     */
    @GetMapping("/search")
    @Operation(summary = "Search for satellite images by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching satellite images",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SatelliteImageDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SatelliteImageDto>> searchSatelliteImagesByName(
            @Parameter(description = "Name to search for") @RequestParam String name) {
        List<SatelliteImageDto> images = satelliteImageService.findByName(name);
        return ResponseEntity.ok(images);
    }

    /**
     * Search for satellite images by date range.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching satellite images
     */
    @GetMapping("/search/date-range")
    @Operation(summary = "Search for satellite images by date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching satellite images",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SatelliteImageDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SatelliteImageDto>> searchSatelliteImagesByDateRange(
            @Parameter(description = "Start date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<SatelliteImageDto> images = satelliteImageService.findByDateRange(startDate, endDate);
        return ResponseEntity.ok(images);
    }

    /**
     * Search for satellite images by geometry.
     *
     * @param wkt The WKT representation of the geometry
     * @return List of matching satellite images
     */
    @GetMapping("/search/geometry")
    @Operation(summary = "Search for satellite images by geometry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching satellite images",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SatelliteImageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid geometry")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SatelliteImageDto>> searchSatelliteImagesByGeometry(
            @Parameter(description = "WKT representation of the geometry") @RequestParam String wkt) {
        try {
            Geometry geometry = new WKTReader().read(wkt);
            List<SatelliteImageDto> images = satelliteImageService.findByIntersectingGeometry(geometry);
            return ResponseEntity.ok(images);
        } catch (ParseException e) {
            throw new GeospatialProcessingException("Invalid WKT geometry: " + e.getMessage(),
                    "SatelliteImage", "search", e);
        }
    }

    /**
     * Upload and create a new satellite image.
     *
     * @param file The satellite image file
     * @param dto The satellite image metadata
     * @return The created satellite image
     * @throws IOException if there is an error processing the file
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and create a new satellite image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Satellite image created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SatelliteImageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SatelliteImageDto> createSatelliteImage(
            @Parameter(description = "Satellite image file") @RequestPart MultipartFile file,
            @Parameter(description = "Satellite image metadata") @RequestPart @Valid SatelliteImageDto dto)
            throws IOException {
        SatelliteImageDto createdImage = satelliteImageService.create(file, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdImage);
    }

    /**
     * Update an existing satellite image.
     *
     * @param id The ID of the satellite image to update
     * @param dto The updated satellite image metadata
     * @return The updated satellite image
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing satellite image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Satellite image updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SatelliteImageDto.class))),
            @ApiResponse(responseCode = "404", description = "Satellite image not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SatelliteImageDto> updateSatelliteImage(
            @Parameter(description = "ID of the satellite image to update") @PathVariable UUID id,
            @Parameter(description = "Updated satellite image metadata") @RequestBody @Valid SatelliteImageDto dto) {
        SatelliteImageDto updatedImage = satelliteImageService.update(id, dto);
        return ResponseEntity.ok(updatedImage);
    }

    /**
     * Delete a satellite image.
     *
     * @param id The ID of the satellite image to delete
     * @return No content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a satellite image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Satellite image deleted"),
            @ApiResponse(responseCode = "404", description = "Satellite image not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSatelliteImage(
            @Parameter(description = "ID of the satellite image to delete") @PathVariable UUID id) {
        satelliteImageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Process a satellite image.
     *
     * @param id The ID of the satellite image to process
     * @return The processed satellite image
     */
    @PostMapping("/{id}/process")
    @Operation(summary = "Process a satellite image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Satellite image processed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SatelliteImageDto.class))),
            @ApiResponse(responseCode = "404", description = "Satellite image not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SatelliteImageDto> processSatelliteImage(
            @Parameter(description = "ID of the satellite image to process") @PathVariable UUID id) {
        SatelliteImageDto processedImage = satelliteImageService.processImage(id);
        return ResponseEntity.ok(processedImage);
    }
}

