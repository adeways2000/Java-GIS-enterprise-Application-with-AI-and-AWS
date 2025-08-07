package com.adeprogramming.javagis.controller;

import com.adeprogramming.javagis.dto.AiModelDto;
import com.adeprogramming.javagis.service.AiModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing AI models.
 * Provides endpoints for CRUD operations on AI models.
 */
@RestController
@RequestMapping("/api/ai/models")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Model API", description = "API for managing AI models")
public class AiModelController {

    private final AiModelService aiModelService;

    /**
     * Get all AI models.
     *
     * @return List of all AI models
     */
    @GetMapping
    @Operation(summary = "Get all AI models")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all AI models",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiModelDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiModelDto>> getAllAiModels() {
        List<AiModelDto> models = aiModelService.findAll();
        return ResponseEntity.ok(models);
    }

    /**
     * Get an AI model by ID.
     *
     * @param id The ID of the AI model
     * @return The AI model
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get an AI model by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the AI model",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiModelDto.class))),
            @ApiResponse(responseCode = "404", description = "AI model not found")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AiModelDto> getAiModelById(
            @Parameter(description = "ID of the AI model to retrieve") @PathVariable UUID id) {
        AiModelDto model = aiModelService.findById(id);
        return ResponseEntity.ok(model);
    }

    /**
     * Search for AI models by name.
     *
     * @param name The name to search for
     * @return List of matching AI models
     */
    @GetMapping("/search")
    @Operation(summary = "Search for AI models by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching AI models",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiModelDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiModelDto>> searchAiModelsByName(
            @Parameter(description = "Name to search for") @RequestParam String name) {
        List<AiModelDto> models = aiModelService.findByName(name);
        return ResponseEntity.ok(models);
    }

    /**
     * Find AI models by type.
     *
     * @param type The model type
     * @return List of matching AI models
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Find AI models by type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found matching AI models",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiModelDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid model type")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiModelDto>> findAiModelsByType(
            @Parameter(description = "Model type") @PathVariable String type) {
        List<AiModelDto> models = aiModelService.findByType(type);
        return ResponseEntity.ok(models);
    }

    /**
     * Find active AI models.
     *
     * @return List of active AI models
     */
    @GetMapping("/active")
    @Operation(summary = "Find active AI models")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found active AI models",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiModelDto.class)))
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AiModelDto>> findActiveAiModels() {
        List<AiModelDto> models = aiModelService.findActiveModels();
        return ResponseEntity.ok(models);
    }

    /**
     * Create a new AI model.
     *
     * @param dto The AI model data
     * @param file The model file (optional)
     * @return The created AI model
     * @throws IOException if there is an error processing the file
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new AI model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "AI model created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiModelDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AiModelDto> createAiModel(
            @Parameter(description = "AI model data") @RequestPart @Valid AiModelDto dto,
            @Parameter(description = "Model file (optional)") @RequestPart(required = false) MultipartFile file)
            throws IOException {
        AiModelDto createdModel = aiModelService.create(dto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdModel);
    }

    /**
     * Update an existing AI model.
     *
     * @param id The ID of the AI model to update
     * @param dto The updated AI model data
     * @param file The updated model file (optional)
     * @return The updated AI model
     * @throws IOException if there is an error processing the file
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update an existing AI model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI model updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AiModelDto.class))),
            @ApiResponse(responseCode = "404", description = "AI model not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AiModelDto> updateAiModel(
            @Parameter(description = "ID of the AI model to update") @PathVariable UUID id,
            @Parameter(description = "Updated AI model data") @RequestPart @Valid AiModelDto dto,
            @Parameter(description = "Updated model file (optional)") @RequestPart(required = false) MultipartFile file)
            throws IOException {
        AiModelDto updatedModel = aiModelService.update(id, dto, file);
        return ResponseEntity.ok(updatedModel);
    }

    /**
     * Delete an AI model.
     *
     * @param id The ID of the AI model to delete
     * @return No content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an AI model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "AI model deleted"),
            @ApiResponse(responseCode = "404", description = "AI model not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAiModel(
            @Parameter(description = "ID of the AI model to delete") @PathVariable UUID id) {
        aiModelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

