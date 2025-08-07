package com.adeprogramming.javagis.controller;

import com.adeprogramming.javagis.dto.AiWorkflowDto;
import com.adeprogramming.javagis.dto.AiAnalysisResultDto;
import com.adeprogramming.javagis.service.AiWorkflowService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing AI workflows.
 * Provides endpoints for CRUD operations and workflow execution.
 */
@RestController
@RequestMapping("/api/ai/workflows")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Workflow API", description = "API for managing AI workflows")
public class AiWorkflowController {

    private final AiWorkflowService aiWorkflowService;

    /**
     * Get all AI workflows.
     *
     * @return List of all AI workflows
     */
    @GetMapping
    @Operation(summary = "Get all AI workflows")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all AI workflows",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiWorkflowDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiWorkflowDto>> getAllAiWorkflows() {
        List<AiWorkflowDto> workflows = aiWorkflowService.findAll();
        return ResponseEntity.ok(workflows);
    }

    /**
     * Get an AI workflow by ID.
     *
     * @param id The ID of the AI workflow
     * @return The AI workflow
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get an AI workflow by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the AI workflow",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiWorkflowDto.class))),
            @ApiResponse(responseCode = "404", description = "AI workflow not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AiWorkflowDto> getAiWorkflowById(
            @Parameter(description = "ID of the AI workflow to retrieve") @PathVariable UUID id) {
        AiWorkflowDto workflow = aiWorkflowService.findById(id);
        return ResponseEntity.ok(workflow);
    }

    /**
     * Search for AI workflows by name.
     *
     * @param name The name to search for
     * @return List of matching AI workflows
     */
    @GetMapping("/search")
    @Operation(summary = "Search for AI workflows by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching AI workflows",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiWorkflowDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiWorkflowDto>> searchAiWorkflowsByName(
            @Parameter(description = "Name to search for") @RequestParam String name) {
        List<AiWorkflowDto> workflows = aiWorkflowService.findByName(name);
        return ResponseEntity.ok(workflows);
    }

    /**
     * Find AI workflows by type.
     *
     * @param type The workflow type
     * @return List of matching AI workflows
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Find AI workflows by type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching AI workflows",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiWorkflowDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid workflow type")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiWorkflowDto>> findAiWorkflowsByType(
            @Parameter(description = "Workflow type") @PathVariable String type) {
        List<AiWorkflowDto> workflows = aiWorkflowService.findByType(type);
        return ResponseEntity.ok(workflows);
    }

    /**
     * Find AI workflows by status.
     *
     * @param status The workflow status
     * @return List of matching AI workflows
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Find AI workflows by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching AI workflows",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiWorkflowDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid workflow status")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiWorkflowDto>> findAiWorkflowsByStatus(
            @Parameter(description = "Workflow status") @PathVariable String status) {
        List<AiWorkflowDto> workflows = aiWorkflowService.findByStatus(status);
        return ResponseEntity.ok(workflows);
    }

    /**
     * Find active AI workflows.
     *
     * @return List of active AI workflows
     */
    @GetMapping("/active")
    @Operation(summary = "Find active AI workflows")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found active AI workflows",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiWorkflowDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiWorkflowDto>> findActiveAiWorkflows() {
        List<AiWorkflowDto> workflows = aiWorkflowService.findActiveWorkflows();
        return ResponseEntity.ok(workflows);
    }

    /**
     * Find AI workflows by area of interest.
     *
     * @param wkt The WKT representation of the geometry
     * @return List of matching AI workflows
     */
    @GetMapping("/area")
    @Operation(summary = "Find AI workflows by area of interest")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching AI workflows",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiWorkflowDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid geometry")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiWorkflowDto>> findAiWorkflowsByAreaOfInterest(
            @Parameter(description = "WKT representation of the geometry") @RequestParam String wkt) {
        try {
            Geometry geometry = new WKTReader().read(wkt);
            List<AiWorkflowDto> workflows = aiWorkflowService.findByAreaOfInterest(geometry);
            return ResponseEntity.ok(workflows);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid WKT geometry: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new AI workflow.
     *
     * @param dto The AI workflow data
     * @return The created AI workflow
     */
    @PostMapping
    @Operation(summary = "Create a new AI workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "AI workflow created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiWorkflowDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AiWorkflowDto> createAiWorkflow(
            @Parameter(description = "AI workflow data") @RequestBody @Valid AiWorkflowDto dto) {
        AiWorkflowDto createdWorkflow = aiWorkflowService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkflow);
    }

    /**
     * Update an existing AI workflow.
     *
     * @param id The ID of the AI workflow to update
     * @param dto The updated AI workflow data
     * @return The updated AI workflow
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing AI workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI workflow updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiWorkflowDto.class))),
            @ApiResponse(responseCode = "404", description = "AI workflow not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AiWorkflowDto> updateAiWorkflow(
            @Parameter(description = "ID of the AI workflow to update") @PathVariable UUID id,
            @Parameter(description = "Updated AI workflow data") @RequestBody @Valid AiWorkflowDto dto) {
        AiWorkflowDto updatedWorkflow = aiWorkflowService.update(id, dto);
        return ResponseEntity.ok(updatedWorkflow);
    }

    /**
     * Delete an AI workflow.
     *
     * @param id The ID of the AI workflow to delete
     * @return No content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an AI workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "AI workflow deleted"),
            @ApiResponse(responseCode = "404", description = "AI workflow not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAiWorkflow(
            @Parameter(description = "ID of the AI workflow to delete") @PathVariable UUID id) {
        aiWorkflowService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Execute an AI workflow.
     *
     * @param id The ID of the AI workflow to execute
     * @return The analysis result
     */
    @PostMapping("/{id}/execute")
    @Operation(summary = "Execute an AI workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI workflow executed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiAnalysisResultDto.class))),
            @ApiResponse(responseCode = "404", description = "AI workflow not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AiAnalysisResultDto> executeAiWorkflow(
            @Parameter(description = "ID of the AI workflow to execute") @PathVariable UUID id) {
        AiAnalysisResultDto result = aiWorkflowService.executeWorkflow(id);
        return ResponseEntity.ok(result);
    }
}

