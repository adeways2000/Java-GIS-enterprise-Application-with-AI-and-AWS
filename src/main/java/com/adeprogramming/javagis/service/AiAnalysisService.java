package com.adeprogramming.javagis.service;

import com.adeprogramming.javagis.domain.ai.AiAnalysisResult;
import com.adeprogramming.javagis.dto.AiAnalysisResultDto;
import com.adeprogramming.javagis.exception.ResourceNotFoundException;
import com.adeprogramming.javagis.repository.AiAnalysisResultRepository;
import com.adeprogramming.javagis.repository.AiWorkflowRepository;
import com.adeprogramming.javagis.service.aws.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing AI analysis results.
 * Provides methods for retrieving and managing analysis results.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisService {

    private final AiAnalysisResultRepository aiAnalysisResultRepository;
    private final AiWorkflowRepository aiWorkflowRepository;
    private final S3Service s3Service;

    /**
     * Find all AI analysis results.
     *
     * @return List of all AI analysis results
     */
    @Transactional(readOnly = true)
    public List<AiAnalysisResultDto> findAll() {
        return aiAnalysisResultRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find an AI analysis result by ID.
     *
     * @param id The ID of the AI analysis result
     * @return The AI analysis result DTO
     * @throws ResourceNotFoundException if the AI analysis result is not found
     */
    @Transactional(readOnly = true)
    public AiAnalysisResultDto findById(UUID id) {
        return aiAnalysisResultRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("AI analysis result not found with id: " + id));
    }

    /**
     * Find AI analysis results by name.
     *
     * @param name The name to search for
     * @return List of matching AI analysis results
     */
    @Transactional(readOnly = true)
    public List<AiAnalysisResultDto> findByName(String name) {
        return aiAnalysisResultRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find AI analysis results by workflow ID.
     *
     * @param workflowId The ID of the workflow
     * @return List of results for the workflow
     */
    @Transactional(readOnly = true)
    public List<AiAnalysisResultDto> findByWorkflowId(UUID workflowId) {
        // Verify workflow exists
        if (!aiWorkflowRepository.existsById(workflowId)) {
            throw new ResourceNotFoundException("AI workflow not found with id: " + workflowId);
        }

        return aiAnalysisResultRepository.findByWorkflowId(workflowId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find AI analysis results by status.
     *
     * @param status The result status
     * @return List of matching AI analysis results
     */
    @Transactional(readOnly = true)
    public List<AiAnalysisResultDto> findByStatus(String status) {
        try {
            AiAnalysisResult.ResultStatus resultStatus = AiAnalysisResult.ResultStatus.valueOf(status.toUpperCase());
            return aiAnalysisResultRepository.findByStatus(resultStatus).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid result status: " + status);
        }
    }

    /**
     * Find AI analysis results by execution date range.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching AI analysis results
     */
    @Transactional(readOnly = true)
    public List<AiAnalysisResultDto> findByExecutionDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return aiAnalysisResultRepository.findByExecutionDateBetween(startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find AI analysis results by area of interest.
     *
     * @param geometry The geometry to check for intersection
     * @return List of matching AI analysis results
     */
    @Transactional(readOnly = true)
    public List<AiAnalysisResultDto> findByAreaOfInterest(Geometry geometry) {
        return aiAnalysisResultRepository.findByIntersectingGeometry(geometry).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find AI analysis results with confidence score greater than or equal to the given value.
     *
     * @param confidenceScore The minimum confidence score
     * @return List of matching AI analysis results
     */
    @Transactional(readOnly = true)
    public List<AiAnalysisResultDto> findByMinConfidenceScore(Double confidenceScore) {
        return aiAnalysisResultRepository.findByConfidenceScoreGreaterThanEqual(confidenceScore).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find the latest AI analysis result for a workflow.
     *
     * @param workflowId The ID of the workflow
     * @return The latest AI analysis result, if any
     */
    @Transactional(readOnly = true)
    public AiAnalysisResultDto findLatestByWorkflowId(UUID workflowId) {
        // Verify workflow exists
        if (!aiWorkflowRepository.existsById(workflowId)) {
            throw new ResourceNotFoundException("AI workflow not found with id: " + workflowId);
        }

        List<AiAnalysisResult> results = aiAnalysisResultRepository.findLatestByWorkflowId(workflowId);
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("No analysis results found for workflow with id: " + workflowId);
        }

        return convertToDto(results.get(0));
    }

    /**
     * Delete an AI analysis result.
     *
     * @param id The ID of the AI analysis result to delete
     * @throws ResourceNotFoundException if the AI analysis result is not found
     */
    @Transactional
    public void delete(UUID id) {
        AiAnalysisResult result = aiAnalysisResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI analysis result not found with id: " + id));

        // Delete result file from S3 if exists
        if (result.getResultPath() != null && result.getResultPath().startsWith("s3://")) {
            String s3Key = result.getResultPath().substring(5); // Remove "s3://"
            s3Key = s3Key.substring(s3Key.indexOf('/') + 1); // Remove bucket name
            s3Service.deleteFile(s3Key);
        }

        aiAnalysisResultRepository.delete(result);
    }

    /**
     * Convert an AiAnalysisResult entity to a DTO.
     *
     * @param result The entity to convert
     * @return The DTO
     */
    private AiAnalysisResultDto convertToDto(AiAnalysisResult result) {
        AiAnalysisResultDto dto = new AiAnalysisResultDto();
        dto.setId(result.getId());
        dto.setName(result.getName());
        dto.setDescription(result.getDescription());
        dto.setWorkflowId(result.getWorkflow().getId());
        dto.setWorkflowName(result.getWorkflow().getName());
        dto.setExecutionDate(result.getExecutionDate());
        dto.setCompletionDate(result.getCompletionDate());
        dto.setStatus(result.getStatus().name());
        dto.setResultPath(result.getResultPath());
        dto.setAreaOfInterest(result.getAreaOfInterest());
        dto.setTimeRangeStart(result.getTimeRangeStart());
        dto.setTimeRangeEnd(result.getTimeRangeEnd());
        dto.setConfidenceScore(result.getConfidenceScore());
        dto.setProcessingTimeMs(result.getProcessingTimeMs());

        if (result.getMetrics() != null) {
            dto.setMetrics(result.getMetrics());
        }

        if (result.getResultData() != null) {
            dto.setResultData(result.getResultData());
        }

        return dto;
    }
}

