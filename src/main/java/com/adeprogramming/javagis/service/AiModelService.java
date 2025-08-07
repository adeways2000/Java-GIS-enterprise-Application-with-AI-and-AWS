package com.adeprogramming.javagis.service;

import com.adeprogramming.javagis.domain.ai.AiModel;
import com.adeprogramming.javagis.dto.AiModelDto;
import com.adeprogramming.javagis.exception.ResourceNotFoundException;
import com.adeprogramming.javagis.repository.AiModelRepository;
import com.adeprogramming.javagis.service.aws.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing AI models.
 * Provides methods for CRUD operations and model management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiModelService {

    private final AiModelRepository aiModelRepository;
    private final S3Service s3Service;

    @Value("${javagis.storage.s3.ai-model-prefix}")
    private String s3ModelPrefix;

    /**
     * Find all AI models.
     *
     * @return List of all AI models
     */
    @Transactional(readOnly = true)
    public List<AiModelDto> findAll() {
        return aiModelRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find an AI model by ID.
     *
     * @param id The ID of the AI model
     * @return The AI model DTO
     * @throws ResourceNotFoundException if the AI model is not found
     */
    @Transactional(readOnly = true)
    public AiModelDto findById(UUID id) {
        return aiModelRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("AI model not found with id: " + id));
    }

    /**
     * Find AI models by name.
     *
     * @param name The name to search for
     * @return List of matching AI models
     */
    @Transactional(readOnly = true)
    public List<AiModelDto> findByName(String name) {
        return aiModelRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find AI models by type.
     *
     * @param type The model type
     * @return List of matching AI models
     */
    @Transactional(readOnly = true)
    public List<AiModelDto> findByType(String type) {
        try {
            AiModel.ModelType modelType = AiModel.ModelType.valueOf(type.toUpperCase());
            return aiModelRepository.findByType(modelType).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid model type: " + type);
        }
    }

    /**
     * Find active AI models.
     *
     * @return List of active AI models
     */
    @Transactional(readOnly = true)
    public List<AiModelDto> findActiveModels() {
        return aiModelRepository.findByIsActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a new AI model.
     *
     * @param dto The AI model data
     * @param file The model file (optional)
     * @return The created AI model DTO
     * @throws IOException if there is an error processing the file
     */
    @Transactional
    public AiModelDto create(AiModelDto dto, MultipartFile file) throws IOException {
        AiModel model = new AiModel();
        model.setName(dto.getName());
        model.setDescription(dto.getDescription());

        try {
            model.setType(AiModel.ModelType.valueOf(dto.getType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid model type: " + dto.getType());
        }

        model.setVersion(dto.getVersion());
        model.setCreatedDate(LocalDateTime.now());
        model.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        model.setAccuracy(dto.getAccuracy());
        model.setTrainingDataset(dto.getTrainingDataset());
        model.setValidationDataset(dto.getValidationDataset());

        if (dto.getParameters() != null) {
            model.setParameters(dto.getParameters());
        }

        if (dto.getMetrics() != null) {
            model.setMetrics(dto.getMetrics());
        }

        // Upload model file to S3 if provided
        if (file != null && !file.isEmpty()) {
            String s3Key = s3ModelPrefix + "/" + UUID.randomUUID() + "/" + file.getOriginalFilename();
            String s3Uri = s3Service.uploadFile(file, s3Key);
            model.setModelPath(s3Uri);
        } else if (dto.getModelPath() != null) {
            model.setModelPath(dto.getModelPath());
        }

        AiModel savedModel = aiModelRepository.save(model);
        return convertToDto(savedModel);
    }

    /**
     * Update an existing AI model.
     *
     * @param id The ID of the AI model to update
     * @param dto The updated AI model data
     * @param file The updated model file (optional)
     * @return The updated AI model DTO
     * @throws ResourceNotFoundException if the AI model is not found
     * @throws IOException if there is an error processing the file
     */
    @Transactional
    public AiModelDto update(UUID id, AiModelDto dto, MultipartFile file) throws IOException {
        AiModel model = aiModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI model not found with id: " + id));

        if (dto.getName() != null) {
            model.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            model.setDescription(dto.getDescription());
        }

        if (dto.getType() != null) {
            try {
                model.setType(AiModel.ModelType.valueOf(dto.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid model type: " + dto.getType());
            }
        }

        if (dto.getVersion() != null) {
            model.setVersion(dto.getVersion());
        }

        model.setLastUpdatedDate(LocalDateTime.now());

        if (dto.getIsActive() != null) {
            model.setIsActive(dto.getIsActive());
        }

        if (dto.getAccuracy() != null) {
            model.setAccuracy(dto.getAccuracy());
        }

        if (dto.getTrainingDataset() != null) {
            model.setTrainingDataset(dto.getTrainingDataset());
        }

        if (dto.getValidationDataset() != null) {
            model.setValidationDataset(dto.getValidationDataset());
        }

        if (dto.getParameters() != null) {
            model.getParameters().clear();
            model.getParameters().putAll(dto.getParameters());
        }

        if (dto.getMetrics() != null) {
            model.getMetrics().clear();
            model.getMetrics().putAll(dto.getMetrics());
        }

        // Upload new model file to S3 if provided
        if (file != null && !file.isEmpty()) {
            // Delete old file if exists
            if (model.getModelPath() != null && model.getModelPath().startsWith("s3://")) {
                String oldS3Key = model.getModelPath().substring(5); // Remove "s3://"
                oldS3Key = oldS3Key.substring(oldS3Key.indexOf('/') + 1); // Remove bucket name
                s3Service.deleteFile(oldS3Key);
            }

            // Upload new file
            String s3Key = s3ModelPrefix + "/" + UUID.randomUUID() + "/" + file.getOriginalFilename();
            String s3Uri = s3Service.uploadFile(file, s3Key);
            model.setModelPath(s3Uri);
        } else if (dto.getModelPath() != null) {
            model.setModelPath(dto.getModelPath());
        }

        AiModel updatedModel = aiModelRepository.save(model);
        return convertToDto(updatedModel);
    }

    /**
     * Delete an AI model.
     *
     * @param id The ID of the AI model to delete
     * @throws ResourceNotFoundException if the AI model is not found
     */
    @Transactional
    public void delete(UUID id) {
        AiModel model = aiModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI model not found with id: " + id));

        // Delete model file from S3 if exists
        if (model.getModelPath() != null && model.getModelPath().startsWith("s3://")) {
            String s3Key = model.getModelPath().substring(5); // Remove "s3://"
            s3Key = s3Key.substring(s3Key.indexOf('/') + 1); // Remove bucket name
            s3Service.deleteFile(s3Key);
        }

        aiModelRepository.delete(model);
    }

    /**
     * Convert an AiModel entity to a DTO.
     *
     * @param model The entity to convert
     * @return The DTO
     */
    private AiModelDto convertToDto(AiModel model) {
        AiModelDto dto = new AiModelDto();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setDescription(model.getDescription());
        dto.setType(model.getType().name());
        dto.setVersion(model.getVersion());
        dto.setCreatedDate(model.getCreatedDate());
        dto.setLastUpdatedDate(model.getLastUpdatedDate());
        dto.setModelPath(model.getModelPath());
        dto.setIsActive(model.getIsActive());
        dto.setAccuracy(model.getAccuracy());
        dto.setTrainingDataset(model.getTrainingDataset());
        dto.setValidationDataset(model.getValidationDataset());

        if (model.getParameters() != null) {
            dto.setParameters(model.getParameters());
        }

        if (model.getMetrics() != null) {
            dto.setMetrics(model.getMetrics());
        }

        return dto;
    }
}
