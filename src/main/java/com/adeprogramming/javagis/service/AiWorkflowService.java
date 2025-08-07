package com.adeprogramming.javagis.service;

import com.adeprogramming.javagis.domain.ai.AiAnalysisResult;
import com.adeprogramming.javagis.domain.ai.AiModel;
import com.adeprogramming.javagis.domain.ai.AiWorkflow;
import com.adeprogramming.javagis.dto.AiAnalysisResultDto;
import com.adeprogramming.javagis.dto.AiWorkflowDto;
import com.adeprogramming.javagis.exception.ResourceNotFoundException;
import com.adeprogramming.javagis.repository.AiAnalysisResultRepository;
import com.adeprogramming.javagis.repository.AiModelRepository;
import com.adeprogramming.javagis.repository.AiWorkflowRepository;
import com.adeprogramming.javagis.service.aws.CloudWatchService;
import com.adeprogramming.javagis.service.aws.LambdaService;
import com.adeprogramming.javagis.service.aws.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing AI workflows.
 * Provides methods for CRUD operations and workflow execution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiWorkflowService {

    private final AiWorkflowRepository aiWorkflowRepository;
    private final AiModelRepository aiModelRepository;
    private final AiAnalysisResultRepository aiAnalysisResultRepository;
    private final LambdaService lambdaService;
    private final S3Service s3Service;
    private final CloudWatchService cloudWatchService;

    /**
     * Find all AI workflows.
     *
     * @return List of all AI workflows
     */
    @Transactional(readOnly = true)
    public List<AiWorkflowDto> findAll() {
        return aiWorkflowRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find an AI workflow by ID.
     *
     * @param id The ID of the AI workflow
     * @return The AI workflow DTO
     * @throws ResourceNotFoundException if the AI workflow is not found
     */
    @Transactional(readOnly = true)
    public AiWorkflowDto findById(UUID id) {
        return aiWorkflowRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("AI workflow not found with id: " + id));
    }

    /**
     * Find AI workflows by name.
     *
     * @param name The name to search for
     * @return List of matching AI workflows
     */
    @Transactional(readOnly = true)
    public List<AiWorkflowDto> findByName(String name) {
        return aiWorkflowRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find AI workflows by type.
     *
     * @param type The workflow type
     * @return List of matching AI workflows
     */
    @Transactional(readOnly = true)
    public List<AiWorkflowDto> findByType(String type) {
        try {
            AiWorkflow.WorkflowType workflowType = AiWorkflow.WorkflowType.valueOf(type.toUpperCase());
            return aiWorkflowRepository.findByType(workflowType).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid workflow type: " + type);
        }
    }

    /**
     * Find AI workflows by status.
     *
     * @param status The workflow status
     * @return List of matching AI workflows
     */
    @Transactional(readOnly = true)
    public List<AiWorkflowDto> findByStatus(String status) {
        try {
            AiWorkflow.WorkflowStatus workflowStatus = AiWorkflow.WorkflowStatus.valueOf(status.toUpperCase());
            return aiWorkflowRepository.findByStatus(workflowStatus).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid workflow status: " + status);
        }
    }

    /**
     * Find active AI workflows.
     *
     * @return List of active AI workflows
     */
    @Transactional(readOnly = true)
    public List<AiWorkflowDto> findActiveWorkflows() {
        return aiWorkflowRepository.findByIsActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find AI workflows by area of interest.
     *
     * @param geometry The geometry to check for intersection
     * @return List of matching AI workflows
     */
    @Transactional(readOnly = true)
    public List<AiWorkflowDto> findByAreaOfInterest(Geometry geometry) {
        return aiWorkflowRepository.findByIntersectingGeometry(geometry).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a new AI workflow.
     *
     * @param dto The AI workflow data
     * @return The created AI workflow DTO
     */
    @Transactional
    public AiWorkflowDto create(AiWorkflowDto dto) {
        AiWorkflow workflow = new AiWorkflow();
        workflow.setName(dto.getName());
        workflow.setDescription(dto.getDescription());

        try {
            workflow.setType(AiWorkflow.WorkflowType.valueOf(dto.getType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid workflow type: " + dto.getType());
        }

        workflow.setStatus(AiWorkflow.WorkflowStatus.CREATED);
        workflow.setCreatedDate(LocalDateTime.now());
        workflow.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        workflow.setAreaOfInterest(dto.getAreaOfInterest());
        workflow.setTimeRangeStart(dto.getTimeRangeStart());
        workflow.setTimeRangeEnd(dto.getTimeRangeEnd());
        workflow.setScheduleCron(dto.getScheduleCron());

        if (dto.getNextScheduledRun() != null) {
            workflow.setNextScheduledRun(dto.getNextScheduledRun());
            workflow.setStatus(AiWorkflow.WorkflowStatus.SCHEDULED);
        }

        // Set workflow steps
        if (dto.getSteps() != null && !dto.getSteps().isEmpty()) {
            List<AiWorkflow.WorkflowStep> steps = new ArrayList<>();
            for (AiWorkflowDto.WorkflowStepDto stepDto : dto.getSteps()) {
                AiWorkflow.WorkflowStep step = new AiWorkflow.WorkflowStep();
                step.setName(stepDto.getName());
                step.setDescription(stepDto.getDescription());
                step.setType(stepDto.getType());
                step.setConfiguration(stepDto.getConfiguration());
                step.setStatus(stepDto.getStatus());
                step.setRetryCount(stepDto.getRetryCount());
                step.setMaxRetries(stepDto.getMaxRetries());
                steps.add(step);
            }
            workflow.setSteps(steps);
        }

        // Set workflow parameters
        if (dto.getParameters() != null) {
            workflow.setParameters(dto.getParameters());
        }

        // Set models
        if (dto.getModelIds() != null && !dto.getModelIds().isEmpty()) {
            List<AiModel> models = new ArrayList<>();
            for (UUID modelId : dto.getModelIds()) {
                AiModel model = aiModelRepository.findById(modelId)
                        .orElseThrow(() -> new ResourceNotFoundException("AI model not found with id: " + modelId));
                models.add(model);
            }
            workflow.setModels(models);
        }

        AiWorkflow savedWorkflow = aiWorkflowRepository.save(workflow);
        return convertToDto(savedWorkflow);
    }

    /**
     * Update an existing AI workflow.
     *
     * @param id The ID of the AI workflow to update
     * @param dto The updated AI workflow data
     * @return The updated AI workflow DTO
     * @throws ResourceNotFoundException if the AI workflow is not found
     */
    @Transactional
    public AiWorkflowDto update(UUID id, AiWorkflowDto dto) {
        AiWorkflow workflow = aiWorkflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI workflow not found with id: " + id));

        if (dto.getName() != null) {
            workflow.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            workflow.setDescription(dto.getDescription());
        }

        if (dto.getType() != null) {
            try {
                workflow.setType(AiWorkflow.WorkflowType.valueOf(dto.getType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid workflow type: " + dto.getType());
            }
        }

        if (dto.getStatus() != null) {
            try {
                workflow.setStatus(AiWorkflow.WorkflowStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid workflow status: " + dto.getStatus());
            }
        }

        if (dto.getIsActive() != null) {
            workflow.setIsActive(dto.getIsActive());
        }

        if (dto.getAreaOfInterest() != null) {
            workflow.setAreaOfInterest(dto.getAreaOfInterest());
        }

        if (dto.getTimeRangeStart() != null) {
            workflow.setTimeRangeStart(dto.getTimeRangeStart());
        }

        if (dto.getTimeRangeEnd() != null) {
            workflow.setTimeRangeEnd(dto.getTimeRangeEnd());
        }

        if (dto.getScheduleCron() != null) {
            workflow.setScheduleCron(dto.getScheduleCron());
        }

        if (dto.getNextScheduledRun() != null) {
            workflow.setNextScheduledRun(dto.getNextScheduledRun());
            if (workflow.getStatus() == AiWorkflow.WorkflowStatus.CREATED) {
                workflow.setStatus(AiWorkflow.WorkflowStatus.SCHEDULED);
            }
        }

        // Update workflow steps
        if (dto.getSteps() != null && !dto.getSteps().isEmpty()) {
            List<AiWorkflow.WorkflowStep> steps = new ArrayList<>();
            for (AiWorkflowDto.WorkflowStepDto stepDto : dto.getSteps()) {
                AiWorkflow.WorkflowStep step = new AiWorkflow.WorkflowStep();
                step.setName(stepDto.getName());
                step.setDescription(stepDto.getDescription());
                step.setType(stepDto.getType());
                step.setConfiguration(stepDto.getConfiguration());
                step.setStatus(stepDto.getStatus());
                step.setRetryCount(stepDto.getRetryCount());
                step.setMaxRetries(stepDto.getMaxRetries());
                steps.add(step);
            }
            workflow.setSteps(steps);
        }

        // Update workflow parameters
        if (dto.getParameters() != null) {
            // Initialize parameters map if it's null
            if (workflow.getParameters() == null) {
                workflow.setParameters(new HashMap<>());
            }
            workflow.getParameters().putAll(dto.getParameters());
        }

        // Update models
        if (dto.getModelIds() != null) {
            List<AiModel> models = new ArrayList<>();
            for (UUID modelId : dto.getModelIds()) {
                AiModel model = aiModelRepository.findById(modelId)
                        .orElseThrow(() -> new ResourceNotFoundException("AI model not found with id: " + modelId));
                models.add(model);
            }
            // Initialize models list if it's null
            if (workflow.getModels() == null) {
                workflow.setModels(new ArrayList<>());
            }
            workflow.getModels().clear();
            workflow.getModels().addAll(models);
        }

        AiWorkflow updatedWorkflow = aiWorkflowRepository.save(workflow);
        return convertToDto(updatedWorkflow);
    }

    /**
     * Delete an AI workflow.
     *
     * @param id The ID of the AI workflow to delete
     * @throws ResourceNotFoundException if the AI workflow is not found
     */
    @Transactional
    public void delete(UUID id) {
        AiWorkflow workflow = aiWorkflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI workflow not found with id: " + id));

        aiWorkflowRepository.delete(workflow);
    }

    /**
     * Execute an AI workflow.
     *
     * @param id The ID of the AI workflow to execute
     * @return The analysis result DTO
     * @throws ResourceNotFoundException if the AI workflow is not found
     */
    @Transactional
    public AiAnalysisResultDto executeWorkflow(UUID id) {
        long startTime = System.currentTimeMillis();

        AiWorkflow workflow = aiWorkflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI workflow not found with id: " + id));

        // Create analysis result
        AiAnalysisResult result = new AiAnalysisResult();
        result.setName("Execution of " + workflow.getName());
        result.setDescription("Analysis result for workflow: " + workflow.getName());
        result.setWorkflow(workflow);
        result.setExecutionDate(LocalDateTime.now());
        result.setStatus(AiAnalysisResult.ResultStatus.PROCESSING);
        result.setAreaOfInterest(workflow.getAreaOfInterest());
        result.setTimeRangeStart(workflow.getTimeRangeStart());
        result.setTimeRangeEnd(workflow.getTimeRangeEnd());

        // Save initial result
        AiAnalysisResult savedResult = aiAnalysisResultRepository.save(result);

        try {
            // Update workflow status
            workflow.setStatus(AiWorkflow.WorkflowStatus.RUNNING);
            workflow.setLastRunDate(LocalDateTime.now());
            aiWorkflowRepository.save(workflow);

            // Execute workflow steps
            boolean success = executeWorkflowSteps(workflow, result);

            // Update result status
            result.setStatus(success ? AiAnalysisResult.ResultStatus.COMPLETED : AiAnalysisResult.ResultStatus.FAILED);
            result.setCompletionDate(LocalDateTime.now());
            result.setProcessingTimeMs(System.currentTimeMillis() - startTime);

            // Update workflow status
            workflow.setStatus(success ? AiWorkflow.WorkflowStatus.COMPLETED : AiWorkflow.WorkflowStatus.FAILED);
            aiWorkflowRepository.save(workflow);

            // Save updated result
            savedResult = aiAnalysisResultRepository.save(result);

            // Monitor execution time
            cloudWatchService.monitorAiWorkflowExecution(workflow.getType().name(), result.getProcessingTimeMs());

            return convertToResultDto(savedResult);
        } catch (Exception e) {
            log.error("Error executing AI workflow: {}", e.getMessage(), e);

            // Update result status
            result.setStatus(AiAnalysisResult.ResultStatus.FAILED);
            result.setCompletionDate(LocalDateTime.now());
            result.setProcessingTimeMs(System.currentTimeMillis() - startTime);

            // Add error information
            if (result.getResultData() == null) {
                result.setResultData(new HashMap<>());
            }
            result.getResultData().put("error", e.getMessage());

            // Update workflow status
            workflow.setStatus(AiWorkflow.WorkflowStatus.FAILED);
            aiWorkflowRepository.save(workflow);

            // Save updated result
            savedResult = aiAnalysisResultRepository.save(result);

            // Monitor execution time
            cloudWatchService.monitorAiWorkflowExecution(workflow.getType().name(), result.getProcessingTimeMs());

            return convertToResultDto(savedResult);
        }
    }

    /**
     * Execute workflow steps.
     *
     * @param workflow The workflow to execute
     * @param result The analysis result to update
     * @return True if all steps executed successfully, false otherwise
     */
    private boolean executeWorkflowSteps(AiWorkflow workflow, AiAnalysisResult result) {
        if (workflow.getSteps() == null || workflow.getSteps().isEmpty()) {
            log.warn("No steps defined for workflow: {}", workflow.getName());
            return false;
        }

        // FIXED: Changed from Map<String, String> to Map<String, Object>
        Map<String, Object> resultData = new HashMap<>();
        List<Map<String, Object>> stepResults = new ArrayList<>();

        for (AiWorkflow.WorkflowStep step : workflow.getSteps()) {
            try {
                log.info("Executing workflow step: {} for workflow: {}", step.getName(), workflow.getName());

                // Update step status
                step.setStatus("RUNNING");

                Map<String, Object> stepResult = executeWorkflowStep(step, workflow, resultData);
                stepResults.add(stepResult);

                // Update step status
                step.setStatus("COMPLETED");

                log.info("Completed workflow step: {} for workflow: {}", step.getName(), workflow.getName());

            } catch (Exception e) {
                log.error("Error executing workflow step: {} for workflow: {}", step.getName(), workflow.getName(), e);

                // Update step status
                step.setStatus("FAILED");

                // Handle retry logic
                if (step.getRetryCount() < step.getMaxRetries()) {
                    step.setRetryCount(step.getRetryCount() + 1);
                    log.info("Retrying workflow step: {} (attempt {} of {})", step.getName(), step.getRetryCount(), step.getMaxRetries());

                    try {
                        Map<String, Object> stepResult = executeWorkflowStep(step, workflow, resultData);
                        stepResults.add(stepResult);
                        step.setStatus("COMPLETED");
                    } catch (Exception retryException) {
                        log.error("Retry failed for workflow step: {}", step.getName(), retryException);
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("stepName", step.getName());
                        errorResult.put("status", "FAILED");
                        errorResult.put("error", retryException.getMessage());
                        stepResults.add(errorResult);
                        return false;
                    }
                } else {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("stepName", step.getName());
                    errorResult.put("status", "FAILED");
                    errorResult.put("error", e.getMessage());
                    stepResults.add(errorResult);
                    return false;
                }
            }
        }

        // Store step results - FIXED: Now storing objects instead of strings
        resultData.put("stepResults", stepResults);
        resultData.put("totalSteps", workflow.getSteps().size());
        resultData.put("completedSteps", stepResults.size());

        result.setResultData(resultData);

        return true;
    }

    /**
     * Execute a single workflow step.
     *
     * @param step The step to execute
     * @param workflow The parent workflow
     * @param contextData The context data from previous steps
     * @return The step execution result
     * @throws Exception if the step execution fails
     */
    // FIXED: Changed parameter type from Map<String, String> to Map<String, Object>
    private Map<String, Object> executeWorkflowStep(AiWorkflow.WorkflowStep step, AiWorkflow workflow, Map<String, Object> contextData) throws Exception {
        Map<String, Object> stepResult = new HashMap<>();
        stepResult.put("stepName", step.getName());
        stepResult.put("stepType", step.getType());
        stepResult.put("startTime", LocalDateTime.now());

        // FIXED: Added null check for step.getType()
        if (step.getType() == null) {
            throw new IllegalArgumentException("Step type cannot be null");
        }

        switch (step.getType().toUpperCase()) {
            case "DATA_COLLECTION":
                Map<String, Object> dataResult = executeDataCollectionStep(step, workflow, contextData);
                stepResult.putAll(dataResult);
                break;
            case "PREPROCESSING":
                Map<String, Object> preprocessResult = executePreprocessingStep(step, workflow, contextData);
                stepResult.putAll(preprocessResult);
                break;
            case "AI_ANALYSIS":
                Map<String, Object> analysisResult = executeAiAnalysisStep(step, workflow, contextData);
                stepResult.putAll(analysisResult);
                break;
            case "POSTPROCESSING":
                Map<String, Object> postprocessResult = executePostprocessingStep(step, workflow, contextData);
                stepResult.putAll(postprocessResult);
                break;
            case "NOTIFICATION":
                Map<String, Object> notificationResult = executeNotificationStep(step, workflow, contextData);
                stepResult.putAll(notificationResult);
                break;
            case "LAMBDA_FUNCTION":
                Map<String, Object> lambdaResult = executeLambdaFunctionStep(step, workflow, contextData);
                stepResult.putAll(lambdaResult);
                break;
            default:
                throw new IllegalArgumentException("Unknown step type: " + step.getType());
        }

        stepResult.put("endTime", LocalDateTime.now());
        stepResult.put("status", "COMPLETED");

        return stepResult;
    }

    /**
     * Execute data collection step.
     */
    // FIXED: Changed parameter type from Map<String, String> to Map<String, Object>
    private Map<String, Object> executeDataCollectionStep(AiWorkflow.WorkflowStep step, AiWorkflow workflow, Map<String, Object> contextData) {
        Map<String, Object> result = new HashMap<>();

        // Simulate data collection
        result.put("dataCollected", true);
        result.put("dataSize", "1.2GB");
        result.put("dataFormat", "GeoTIFF");
        result.put("dataSource", "Sentinel-2");

        log.info("Data collection completed for step: {}", step.getName());

        return result;
    }

    /**
     * Execute preprocessing step.
     */
    // FIXED: Changed parameter type from Map<String, String> to Map<String, Object>
    private Map<String, Object> executePreprocessingStep(AiWorkflow.WorkflowStep step, AiWorkflow workflow, Map<String, Object> contextData) {
        Map<String, Object> result = new HashMap<>();

        // Simulate preprocessing
        result.put("preprocessingCompleted", true);
        result.put("cloudMasking", true);
        result.put("atmosphericCorrection", true);
        result.put("geometricCorrection", true);

        log.info("Preprocessing completed for step: {}", step.getName());

        return result;
    }

    /**
     * Execute AI analysis step.
     */
    // FIXED: Changed parameter type from Map<String, String> to Map<String, Object>
    private Map<String, Object> executeAiAnalysisStep(AiWorkflow.WorkflowStep step, AiWorkflow workflow, Map<String, Object> contextData) {
        Map<String, Object> result = new HashMap<>();

        // FIXED: Added null check for workflow.getType()
        if (workflow.getType() != null) {
            // Simulate AI analysis based on workflow type
            switch (workflow.getType()) {
                case ENVIRONMENTAL_MONITORING:
                    result.put("analysisType", "Environmental Monitoring");
                    result.put("ndviMean", 0.65);
                    result.put("vegetationHealth", "Good");
                    result.put("anomaliesDetected", 2);
                    break;
                case PREDICTIVE_MAINTENANCE:
                    result.put("analysisType", "Predictive Maintenance");
                    result.put("equipmentHealth", "Normal");
                    result.put("maintenanceRequired", false);
                    result.put("riskScore", 0.15);
                    break;
                case ANOMALY_DETECTION:
                    result.put("analysisType", "Anomaly Detection");
                    result.put("anomaliesFound", 3);
                    result.put("confidenceScore", 0.87);
                    result.put("alertLevel", "Medium");
                    break;
                default:
                    result.put("analysisType", "Generic Analysis");
                    result.put("analysisCompleted", true);
            }
        } else {
            result.put("analysisType", "Generic Analysis");
            result.put("analysisCompleted", true);
        }

        log.info("AI analysis completed for step: {}", step.getName());

        return result;
    }

    /**
     * Execute postprocessing step.
     */
    // FIXED: Changed parameter type from Map<String, String> to Map<String, Object>
    private Map<String, Object> executePostprocessingStep(AiWorkflow.WorkflowStep step, AiWorkflow workflow, Map<String, Object> contextData) {
        Map<String, Object> result = new HashMap<>();

        // Simulate postprocessing
        result.put("postprocessingCompleted", true);
        result.put("reportGenerated", true);
        result.put("visualizationCreated", true);
        result.put("outputFormat", "PDF");

        log.info("Postprocessing completed for step: {}", step.getName());

        return result;
    }

    /**
     * Execute notification step.
     */
    // FIXED: Changed parameter type from Map<String, String> to Map<String, Object>
    private Map<String, Object> executeNotificationStep(AiWorkflow.WorkflowStep step, AiWorkflow workflow, Map<String, Object> contextData) {
        Map<String, Object> result = new HashMap<>();

        // Simulate notification
        result.put("notificationSent", true);
        result.put("notificationType", "Email");
        result.put("recipients", Arrays.asList("admin@basf.com", "analyst@basf.com"));

        log.info("Notification sent for step: {}", step.getName());

        return result;
    }

    /**
     * Execute Lambda function step - COMPLETELY FIXED VERSION
     */
    private Map<String, Object> executeLambdaFunctionStep(AiWorkflow.WorkflowStep step, AiWorkflow workflow, Map<String, Object> contextData) throws Exception {
        Map<String, Object> result = new HashMap<>();

// Check if step configuration exists
        if (step.getConfiguration() == null) {
            throw new IllegalArgumentException("Step configuration is null for Lambda function step");
        }

// Get configuration as String (since that's what the method returns)
        String configString = step.getConfiguration();
        String functionName = null;
        Map<String, Object> stepConfig = new HashMap<>();

// Try to parse configuration string to extract function name
        try {
            // Option 1: Try parsing as JSON
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> parsedConfig = objectMapper.readValue(configString, Map.class);
            functionName = (String) parsedConfig.get("functionName");
            stepConfig.putAll(parsedConfig);
        } catch (Exception e) {
            // Option 2: If not JSON, check for simple patterns
            if (configString.contains("functionName")) {
                // Extract from patterns like "functionName=myFunction" or "functionName:myFunction"
                String[] parts = configString.split("[=:]");
                if (parts.length >= 2) {
                    functionName = parts[1].trim().replaceAll("[\"',}\\s]", "");
                }
            } else {
                // Option 3: Treat entire string as function name
                functionName = configString.trim();
            }

            // Store original config
            stepConfig.put("functionName", functionName);
            stepConfig.put("originalConfig", configString);
        }

// Validate function name
        if (functionName == null || functionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Lambda function name not found in configuration: " + configString);
        }

// Prepare payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("workflowId", workflow.getId().toString());
        payload.put("stepName", step.getName());
        payload.put("contextData", contextData);
        payload.putAll(stepConfig);


        // Safely add step configuration to payload
        try {
            // Create a copy of the configuration to avoid modifying the original
            Map<String, Object> configCopy = new HashMap<>(stepConfig);
            payload.putAll(configCopy);
        } catch (Exception e) {
            log.warn("Failed to add step configuration to payload: {}", e.getMessage());
            // Add configuration as a nested object instead
            payload.put("stepConfiguration", stepConfig);
        }

        try {
            // Convert payload to JSON string using ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            String payloadJson = objectMapper.writeValueAsString(payload);

            // Invoke Lambda function using the correct method from LambdaService
            String lambdaResponse = lambdaService.invokeLambdaFunction(functionName, payloadJson);

            // Parse the response
            if (lambdaResponse != null && !lambdaResponse.isEmpty()) {
                result.put("lambdaResponse", lambdaResponse);
                result.put("success", true);
                result.put("functionName", functionName);
            } else {
                result.put("success", false);
                result.put("error", "Empty response from Lambda function");
                result.put("functionName", functionName);
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to invoke Lambda function: " + e.getMessage());
            result.put("functionName", functionName);
            log.error("Error invoking Lambda function {}: {}", functionName, e.getMessage(), e);
        }

        log.info("Lambda function {} executed for step: {}", functionName, step.getName());

        return result;
    }

    /**
     * Schedule workflow execution.
     *
     * @param id The ID of the AI workflow to schedule
     * @param scheduledTime The time to schedule the execution
     * @return The updated AI workflow DTO
     */
    @Transactional
    public AiWorkflowDto scheduleWorkflow(UUID id, LocalDateTime scheduledTime) {
        AiWorkflow workflow = aiWorkflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI workflow not found with id: " + id));

        workflow.setNextScheduledRun(scheduledTime);
        workflow.setStatus(AiWorkflow.WorkflowStatus.SCHEDULED);

        AiWorkflow updatedWorkflow = aiWorkflowRepository.save(workflow);
        return convertToDto(updatedWorkflow);
    }

    /**
     * Cancel a scheduled workflow.
     *
     * @param id The ID of the AI workflow to cancel
     * @return The updated AI workflow DTO
     */
    @Transactional
    public AiWorkflowDto cancelScheduledWorkflow(UUID id) {
        AiWorkflow workflow = aiWorkflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI workflow not found with id: " + id));

        workflow.setNextScheduledRun(null);
        workflow.setStatus(AiWorkflow.WorkflowStatus.CREATED);

        AiWorkflow updatedWorkflow = aiWorkflowRepository.save(workflow);
        return convertToDto(updatedWorkflow);
    }

    /**
     * Get workflow execution history.
     *
     * @param id The ID of the AI workflow
     * @return List of analysis results for the workflow
     */
    @Transactional(readOnly = true)
    public List<AiAnalysisResultDto> getWorkflowExecutionHistory(UUID id) {
        AiWorkflow workflow = aiWorkflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI workflow not found with id: " + id));

        // FIXED: Changed from findByExecutionDateBetween to findByWorkflowOrderByExecutionDateDesc
        List<AiAnalysisResult> results = aiAnalysisResultRepository.findByWorkflowOrderByExecutionDateDesc(workflow);

        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        return results.stream()
                .map(this::convertToResultDto)
                .collect(Collectors.toList());
    }

    /**
     * Scheduled method to execute workflows.
     * Runs every minute to check for scheduled workflows.
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void executeScheduledWorkflows() {
        LocalDateTime now = LocalDateTime.now();
        List<AiWorkflow> scheduledWorkflows = aiWorkflowRepository.findScheduledWorkflows(now);

        // FIXED: Added null check for scheduledWorkflows
        if (scheduledWorkflows != null && !scheduledWorkflows.isEmpty()) {
            for (AiWorkflow workflow : scheduledWorkflows) {
                try {
                    log.info("Executing scheduled workflow: {}", workflow.getName());
                    executeWorkflow(workflow.getId());

                    // Update next scheduled run if it's a recurring workflow
                    if (workflow.getScheduleCron() != null && !workflow.getScheduleCron().isEmpty()) {
                        // Calculate next run time based on cron expression
                        // This is a simplified implementation - in production, use a proper cron parser
                        LocalDateTime nextRun = calculateNextRunTime(workflow.getScheduleCron(), now);
                        workflow.setNextScheduledRun(nextRun);
                        aiWorkflowRepository.save(workflow);
                    }

                } catch (Exception e) {
                    log.error("Error executing scheduled workflow: {}", workflow.getName(), e);
                }
            }
        }
    }

    /**
     * Calculate next run time based on cron expression.
     * This is a simplified implementation.
     */
    private LocalDateTime calculateNextRunTime(String cronExpression, LocalDateTime currentTime) {
        // Simplified cron parsing - in production, use a proper cron library
        // For now, just add 24 hours for daily execution
        return currentTime.plusDays(1);
    }

    /**
     * Convert AiWorkflow entity to DTO.
     */
    private AiWorkflowDto convertToDto(AiWorkflow workflow) {
        AiWorkflowDto dto = new AiWorkflowDto();
        dto.setId(workflow.getId());
        dto.setName(workflow.getName());
        dto.setDescription(workflow.getDescription());
        // FIXED: Added null checks for type and status
        dto.setType(workflow.getType() != null ? workflow.getType().name() : null);
        dto.setStatus(workflow.getStatus() != null ? workflow.getStatus().name() : null);
        dto.setCreatedDate(workflow.getCreatedDate());
        dto.setLastRunDate(workflow.getLastRunDate());
        dto.setNextScheduledRun(workflow.getNextScheduledRun());
        dto.setIsActive(workflow.getIsActive());
        dto.setAreaOfInterest(workflow.getAreaOfInterest());
        dto.setTimeRangeStart(workflow.getTimeRangeStart());
        dto.setTimeRangeEnd(workflow.getTimeRangeEnd());
        dto.setScheduleCron(workflow.getScheduleCron());
        dto.setParameters(workflow.getParameters());

        // Convert workflow steps
        if (workflow.getSteps() != null) {
            List<AiWorkflowDto.WorkflowStepDto> stepDtos = new ArrayList<>();
            for (AiWorkflow.WorkflowStep step : workflow.getSteps()) {
                AiWorkflowDto.WorkflowStepDto stepDto = new AiWorkflowDto.WorkflowStepDto();
                stepDto.setName(step.getName());
                stepDto.setDescription(step.getDescription());
                stepDto.setType(step.getType());
                stepDto.setConfiguration(step.getConfiguration());
                stepDto.setStatus(step.getStatus());
                stepDto.setRetryCount(step.getRetryCount());
                stepDto.setMaxRetries(step.getMaxRetries());
                stepDtos.add(stepDto);
            }
            dto.setSteps(stepDtos);
        }

        // Convert model IDs
        if (workflow.getModels() != null) {
            List<UUID> modelIds = workflow.getModels().stream()
                    .map(AiModel::getId)
                    .collect(Collectors.toList());
            dto.setModelIds(modelIds);
        }

        return dto;
    }

    /**
     * Convert AiAnalysisResult entity to DTO.
     */
    private AiAnalysisResultDto convertToResultDto(AiAnalysisResult result) {
        AiAnalysisResultDto dto = new AiAnalysisResultDto();
        dto.setId(result.getId());
        dto.setName(result.getName());
        dto.setDescription(result.getDescription());
        dto.setExecutionDate(result.getExecutionDate());
        dto.setCompletionDate(result.getCompletionDate());
        // FIXED: Added null check for status
        dto.setStatus(result.getStatus() != null ? result.getStatus().name() : null);
        dto.setProcessingTimeMs(result.getProcessingTimeMs());
        dto.setAreaOfInterest(result.getAreaOfInterest());
        dto.setTimeRangeStart(result.getTimeRangeStart());
        dto.setTimeRangeEnd(result.getTimeRangeEnd());
        dto.setResultData(result.getResultData());
        dto.setOutputFiles(result.getOutputFiles());
        dto.setConfidenceScore(result.getConfidenceScore());
        dto.setQualityMetrics(result.getQualityMetrics());

        if (result.getWorkflow() != null) {
            dto.setWorkflowId(result.getWorkflow().getId());
            dto.setWorkflowName(result.getWorkflow().getName());
        }

        return dto;
    }
}
