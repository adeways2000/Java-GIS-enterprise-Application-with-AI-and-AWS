package com.adeprogramming.javagis.controller;

import com.adeprogramming.javagis.dto.AiAnalysisResultDto;
import com.adeprogramming.javagis.service.AiAnalysisService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing AI analysis results.
 * Provides endpoints for retrieving and managing analysis results.
 */
@RestController
@RequestMapping("/api/ai/results")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Analysis Result API", description = "API for managing AI analysis results")
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    /**
     * Get all AI analysis results.
     *
     * @return List of all AI analysis results
     */
    @GetMapping
    @Operation(summary = "Get all AI analysis results")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all AI analysis results",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiAnalysisResultDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiAnalysisResultDto>> getAllAiAnalysisResults() {
        List<AiAnalysisResultDto> results = aiAnalysisService.findAll();
        return ResponseEntity.ok(results);
    }

    /**
     * Get an AI analysis result by ID.
     *
     * @param id The ID of the AI analysis result
     * @return The AI analysis result
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get an AI analysis result by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the AI analysis result",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiAnalysisResultDto.class))),
            @ApiResponse(responseCode = "404", description = "AI analysis result not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AiAnalysisResultDto> getAiAnalysisResultById(
            @Parameter(description = "ID of the AI analysis result to retrieve") @PathVariable UUID id) {
        AiAnalysisResultDto result = aiAnalysisService.findById(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Search for AI analysis results by name.
     *
     * @param name The name to search for
     * @return List of matching AI analysis results
     */
    @GetMapping("/search")
    @Operation(summary = "Search for AI analysis results by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching AI analysis results",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiAnalysisResultDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiAnalysisResultDto>> searchAiAnalysisResultsByName(
            @Parameter(description = "Name to search for") @RequestParam String name) {
        List<AiAnalysisResultDto> results = aiAnalysisService.findByName(name);
        return ResponseEntity.ok(results);
    }

    /**
     * Find AI analysis results by workflow ID.
     *
     * @param workflowId The ID of the workflow
     * @return List of results for the workflow
     */
    @GetMapping("/workflow/{workflowId}")
    @Operation(summary = "Find AI analysis results by workflow ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching AI analysis results",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiAnalysisResultDto.class))),
            @ApiResponse(responseCode = "404", description = "AI workflow not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiAnalysisResultDto>> findAiAnalysisResultsByWorkflowId(
            @Parameter(description = "ID of the workflow") @PathVariable UUID workflowId) {
        List<AiAnalysisResultDto> results = aiAnalysisService.findByWorkflowId(workflowId);
        return ResponseEntity.ok(results);
    }

    /**
     * Find the latest AI analysis result for a workflow.
     *
     * @param workflowId The ID of the workflow
     * @return The latest AI analysis result
     */
    @GetMapping("/workflow/{workflowId}/latest")
    @Operation(summary = "Find the latest AI analysis result for a workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the latest AI analysis result",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiAnalysisResultDto.class))),
            @ApiResponse(responseCode = "404", description = "AI workflow not found or no results available")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AiAnalysisResultDto> findLatestAiAnalysisResultByWorkflowId(
            @Parameter(description = "ID of the workflow") @PathVariable UUID workflowId) {
        AiAnalysisResultDto result = aiAnalysisService.findLatestByWorkflowId(workflowId);
        return ResponseEntity.ok(result);
    }

    /**
     * Find AI analysis results by status.
     *
     * @param status The result status
     * @return List of matching AI analysis results
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Find AI analysis results by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching AI analysis results",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiAnalysisResultDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid result status")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiAnalysisResultDto>> findAiAnalysisResultsByStatus(
            @Parameter(description = "Result status") @PathVariable String status) {
        List<AiAnalysisResultDto> results = aiAnalysisService.findByStatus(status);
        return ResponseEntity.ok(results);
    }

    /**
     * Find AI analysis results by execution date range.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching AI analysis results
     */
    @GetMapping("/date-range")
    @Operation(summary = "Find AI analysis results by execution date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching AI analysis results",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiAnalysisResultDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiAnalysisResultDto>> findAiAnalysisResultsByDateRange(
            @Parameter(description = "Start date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AiAnalysisResultDto> results = aiAnalysisService.findByExecutionDateRange(startDate, endDate);
        return ResponseEntity.ok(results);
    }

    /**
     * Find AI analysis results by area of interest.
     *
     * @param wkt The WKT representation of the geometry
     * @return List of matching AI analysis results
     */
    @GetMapping("/area")
    @Operation(summary = "Find AI analysis results by area of interest")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching AI analysis results",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiAnalysisResultDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid geometry")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiAnalysisResultDto>> findAiAnalysisResultsByAreaOfInterest(
            @Parameter(description = "WKT representation of the geometry") @RequestParam String wkt) {
        try {
            Geometry geometry = new WKTReader().read(wkt);
            List<AiAnalysisResultDto> results = aiAnalysisService.findByAreaOfInterest(geometry);
            return ResponseEntity.ok(results);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid WKT geometry: " + e.getMessage(), e);
        }
    }

    /**
     * Find AI analysis results with minimum confidence score.
     *
     * @param confidenceScore The minimum confidence score
     * @return List of matching AI analysis results
     */
    @GetMapping("/confidence")
    @Operation(summary = "Find AI analysis results with minimum confidence score")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching AI analysis results",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiAnalysisResultDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiAnalysisResultDto>> findAiAnalysisResultsByMinConfidenceScore(
            @Parameter(description = "Minimum confidence score") @RequestParam Double confidenceScore) {
        List<AiAnalysisResultDto> results = aiAnalysisService.findByMinConfidenceScore(confidenceScore);
        return ResponseEntity.ok(results);
    }

    /**
     * Delete an AI analysis result.
     *
     * @param id The ID of the AI analysis result to delete
     * @return No content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an AI analysis result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "AI analysis result deleted"),
            @ApiResponse(responseCode = "404", description = "AI analysis result not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAiAnalysisResult(
            @Parameter(description = "ID of the AI analysis result to delete") @PathVariable UUID id) {
        aiAnalysisService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

