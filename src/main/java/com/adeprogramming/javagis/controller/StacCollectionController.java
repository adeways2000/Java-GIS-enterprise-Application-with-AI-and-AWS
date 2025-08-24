package com.adeprogramming.javagis.controller;

import com.adeprogramming.javagis.dto.StacCollectionDto;
import com.adeprogramming.javagis.service.StacCollectionService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing STAC collections.
 * Provides endpoints for CRUD operations and search on STAC collections.
 */
@RestController
@RequestMapping("/api/stac/collections")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "STAC Collection API", description = "API for managing STAC collections")
public class StacCollectionController {

    private final StacCollectionService stacCollectionService;

    /**
     * Get all STAC collections.
     *
     * @return List of all STAC collections
     */
    @GetMapping
    @Operation(summary = "Get all STAC collections")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all STAC collections",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StacCollectionDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<StacCollectionDto>> getAllStacCollections() {
        List<StacCollectionDto> collections = stacCollectionService.findAll();
        return ResponseEntity.ok(collections);
    }

    /**
     * Get a STAC collection by ID.
     *
     * @param id The ID of the STAC collection
     * @return The STAC collection
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a STAC collection by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the STAC collection",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StacCollectionDto.class))),
            @ApiResponse(responseCode = "404", description = "STAC collection not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StacCollectionDto> getStacCollectionById(
            @Parameter(description = "ID of the STAC collection to retrieve") @PathVariable UUID id) {
        StacCollectionDto collection = stacCollectionService.findById(id);
        return ResponseEntity.ok(collection);
    }

    /**
     * Get a STAC collection by STAC ID.
     *
     * @param stacId The STAC ID of the collection
     * @return The STAC collection
     */
    @GetMapping("/stac/{stacId}")
    @Operation(summary = "Get a STAC collection by STAC ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the STAC collection",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StacCollectionDto.class))),
            @ApiResponse(responseCode = "404", description = "STAC collection not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StacCollectionDto> getStacCollectionByStacId(
            @Parameter(description = "STAC ID of the collection to retrieve") @PathVariable String stacId) {
        StacCollectionDto collection = stacCollectionService.findByStacId(stacId);
        return ResponseEntity.ok(collection);
    }

    /**
     * Search for STAC collections by title.
     *
     * @param title The title to search for
     * @return List of matching STAC collections
     */
    @GetMapping("/search")
    @Operation(summary = "Search for STAC collections by title")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching STAC collections",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StacCollectionDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<StacCollectionDto>> searchStacCollectionsByTitle(
            @Parameter(description = "Title to search for") @RequestParam String title) {
        List<StacCollectionDto> collections = stacCollectionService.findByTitle(title);
        return ResponseEntity.ok(collections);
    }

    /**
     * Search for STAC collections by temporal extent.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching STAC collections
     */
    @GetMapping("/search/temporal")
    @Operation(summary = "Search for STAC collections by temporal extent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching STAC collections",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StacCollectionDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<StacCollectionDto>> searchStacCollectionsByTemporalExtent(
            @Parameter(description = "Start date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<StacCollectionDto> collections = stacCollectionService.findByTemporalExtent(startDate, endDate);
        return ResponseEntity.ok(collections);
    }

    /**
     * Search for STAC collections by spatial extent.
     *
     * @param wkt The WKT representation of the geometry
     * @return List of matching STAC collections
     */
    @GetMapping("/search/spatial")
    @Operation(summary = "Search for STAC collections by spatial extent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching STAC collections",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StacCollectionDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid geometry")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<StacCollectionDto>> searchStacCollectionsBySpatialExtent(
            @Parameter(description = "WKT representation of the geometry") @RequestParam String wkt) {
        try {
            Geometry geometry = new WKTReader().read(wkt);
            List<StacCollectionDto> collections = stacCollectionService.findByIntersectingGeometry(geometry);
            return ResponseEntity.ok(collections);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid WKT geometry: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new STAC collection.
     *
     * @param dto The STAC collection to create
     * @return The created STAC collection
     */
    @PostMapping
    @Operation(summary = "Create a new STAC collection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "STAC collection created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StacCollectionDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StacCollectionDto> createStacCollection(
            @Parameter(description = "STAC collection to create") @RequestBody @Valid StacCollectionDto dto) {
        StacCollectionDto createdCollection = stacCollectionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCollection);
    }

    /**
     * Update an existing STAC collection.
     *
     * @param id The ID of the STAC collection to update
     * @param dto The updated STAC collection
     * @return The updated STAC collection
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing STAC collection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "STAC collection updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StacCollectionDto.class))),
            @ApiResponse(responseCode = "404", description = "STAC collection not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StacCollectionDto> updateStacCollection(
            @Parameter(description = "ID of the STAC collection to update") @PathVariable UUID id,
            @Parameter(description = "Updated STAC collection") @RequestBody @Valid StacCollectionDto dto) {
        StacCollectionDto updatedCollection = stacCollectionService.update(id, dto);
        return ResponseEntity.ok(updatedCollection);
    }

    /**
     * Delete a STAC collection.
     *
     * @param id The ID of the STAC collection to delete
     * @return No content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a STAC collection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "STAC collection deleted"),
            @ApiResponse(responseCode = "404", description = "STAC collection not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStacCollection(
            @Parameter(description = "ID of the STAC collection to delete") @PathVariable UUID id) {
        stacCollectionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get STAC collection metadata in STAC format.
     *
     * @param id The ID of the STAC collection
     * @return The STAC collection metadata
     */
    @GetMapping("/{id}/stac")
    @Operation(summary = "Get STAC collection metadata in STAC format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the STAC collection metadata"),
            @ApiResponse(responseCode = "404", description = "STAC collection not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getStacCollectionMetadata(
            @Parameter(description = "ID of the STAC collection") @PathVariable UUID id) {
        Map<String, Object> metadata = stacCollectionService.getStacMetadata(id);
        return ResponseEntity.ok(metadata);
    }
}

