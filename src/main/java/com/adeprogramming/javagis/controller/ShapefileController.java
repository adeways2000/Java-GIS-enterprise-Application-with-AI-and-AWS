package com.adeprogramming.javagis.controller;

import com.adeprogramming.javagis.dto.ShapefileDto;
import com.adeprogramming.javagis.exception.GeospatialProcessingException;
import com.adeprogramming.javagis.service.ShapefileService;
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
 * REST controller for managing shapefiles.
 * Provides endpoints for CRUD operations and spatial queries on shapefiles.
 */
@RestController
@RequestMapping("/api/shapefiles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shapefile API", description = "API for managing shapefiles")
public class ShapefileController {

    private final ShapefileService shapefileService;

    /**
     * Get all shapefiles.
     *
     * @return List of all shapefiles
     */
    @GetMapping
    @Operation(summary = "Get all shapefiles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all shapefiles",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShapefileDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ShapefileDto>> getAllShapefiles() {
        List<ShapefileDto> shapefiles = shapefileService.findAll();
        return ResponseEntity.ok(shapefiles);
    }

    /**
     * Get a shapefile by ID.
     *
     * @param id The ID of the shapefile
     * @return The shapefile
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a shapefile by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the shapefile",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShapefileDto.class))),
            @ApiResponse(responseCode = "404", description = "Shapefile not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ShapefileDto> getShapefileById(
            @Parameter(description = "ID of the shapefile to retrieve") @PathVariable UUID id) {
        ShapefileDto shapefile = shapefileService.findById(id);
        return ResponseEntity.ok(shapefile);
    }

    /**
     * Search for shapefiles by name.
     *
     * @param name The name to search for
     * @return List of matching shapefiles
     */
    @GetMapping("/search")
    @Operation(summary = "Search for shapefiles by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching shapefiles",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShapefileDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ShapefileDto>> searchShapefilesByName(
            @Parameter(description = "Name to search for") @RequestParam String name) {
        List<ShapefileDto> shapefiles = shapefileService.findByName(name);
        return ResponseEntity.ok(shapefiles);
    }

    /**
     * Search for shapefiles by date range.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching shapefiles
     */
    @GetMapping("/search/date-range")
    @Operation(summary = "Search for shapefiles by date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching shapefiles",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShapefileDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ShapefileDto>> searchShapefilesByDateRange(
            @Parameter(description = "Start date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ShapefileDto> shapefiles = shapefileService.findByDateRange(startDate, endDate);
        return ResponseEntity.ok(shapefiles);
    }

    /**
     * Search for shapefiles by geometry.
     *
     * @param wkt The WKT representation of the geometry
     * @return List of matching shapefiles
     */
    @GetMapping("/search/geometry")
    @Operation(summary = "Search for shapefiles by geometry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching shapefiles",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShapefileDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid geometry")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ShapefileDto>> searchShapefilesByGeometry(
            @Parameter(description = "WKT representation of the geometry") @RequestParam String wkt) {
        try {
            Geometry geometry = new WKTReader().read(wkt);
            List<ShapefileDto> shapefiles = shapefileService.findByIntersectingGeometry(geometry);
            return ResponseEntity.ok(shapefiles);
        } catch (ParseException e) {
            throw new GeospatialProcessingException("Invalid WKT geometry: " + e.getMessage(),
                    "Shapefile", "search", e);
        }
    }

    /**
     * Upload and create a new shapefile.
     *
     * @param file The shapefile zip file
     * @param dto The shapefile metadata
     * @return The created shapefile
     * @throws IOException if there is an error processing the file
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and create a new shapefile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Shapefile created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShapefileDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShapefileDto> createShapefile(
            @Parameter(description = "Shapefile zip file") @RequestPart MultipartFile file,
            @Parameter(description = "Shapefile metadata") @RequestPart @Valid ShapefileDto dto)
            throws IOException {
        ShapefileDto createdShapefile = shapefileService.create(file, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdShapefile);
    }

    /**
     * Update an existing shapefile.
     *
     * @param id The ID of the shapefile to update
     * @param dto The updated shapefile metadata
     * @return The updated shapefile
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing shapefile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shapefile updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShapefileDto.class))),
            @ApiResponse(responseCode = "404", description = "Shapefile not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShapefileDto> updateShapefile(
            @Parameter(description = "ID of the shapefile to update") @PathVariable UUID id,
            @Parameter(description = "Updated shapefile metadata") @RequestBody @Valid ShapefileDto dto) {
        ShapefileDto updatedShapefile = shapefileService.update(id, dto);
        return ResponseEntity.ok(updatedShapefile);
    }

    /**
     * Delete a shapefile.
     *
     * @param id The ID of the shapefile to delete
     * @return No content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a shapefile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Shapefile deleted"),
            @ApiResponse(responseCode = "404", description = "Shapefile not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteShapefile(
            @Parameter(description = "ID of the shapefile to delete") @PathVariable UUID id) {
        shapefileService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Process a shapefile.
     *
     * @param id The ID of the shapefile to process
     * @return The processed shapefile
     */
    @PostMapping("/{id}/process")
    @Operation(summary = "Process a shapefile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shapefile processed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShapefileDto.class))),
            @ApiResponse(responseCode = "404", description = "Shapefile not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShapefileDto> processShapefile(
            @Parameter(description = "ID of the shapefile to process") @PathVariable UUID id) {
        ShapefileDto processedShapefile = shapefileService.processShapefile(id);
        return ResponseEntity.ok(processedShapefile);
    }
}
