package com.adeprogramming.javagis.service.aws;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for interacting with Amazon CloudWatch.
 * Provides methods for publishing metrics, creating alarms, and monitoring application performance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudWatchService {

    private final AmazonCloudWatch amazonCloudWatch;

    @Value("${spring.application.name:javagis-basf}")
    private String applicationName;

    /**
     * Publish a custom metric to CloudWatch.
     *
     * @param metricName The name of the metric
     * @param value The value of the metric
     * @param unit The unit of the metric
     * @param dimensions Additional dimensions for the metric
     */
    public void publishMetric(String metricName, double value, StandardUnit unit, Map<String, String> dimensions) {
        log.info("Publishing metric: {} with value: {} {}", metricName, value, unit);

        Dimension applicationDimension = new Dimension()
                .withName("Application")
                .withValue(applicationName);

        MetricDatum datum = new MetricDatum()
                .withMetricName(metricName)
                .withValue(value)
                .withUnit(unit)
                .withTimestamp(new Date());

        // Add application dimension
        datum.withDimensions(applicationDimension);

        // Add custom dimensions
        if (dimensions != null) {
            for (Map.Entry<String, String> entry : dimensions.entrySet()) {
                Dimension dimension = new Dimension()
                        .withName(entry.getKey())
                        .withValue(entry.getValue());
                datum.withDimensions(dimension);
            }
        }

        PutMetricDataRequest request = new PutMetricDataRequest()
                .withNamespace("BASF/JavaGIS")
                .withMetricData(datum);

        try {
            amazonCloudWatch.putMetricData(request);
            log.debug("Successfully published metric: {}", metricName);
        } catch (Exception e) {
            log.error("Error publishing metric: {}", e.getMessage(), e);
        }
    }

    /**
     * Create a CloudWatch alarm for a metric.
     *
     * @param alarmName The name of the alarm
     * @param metricName The name of the metric to monitor
     * @param threshold The threshold value for the alarm
     * @param comparisonOperator The comparison operator for the threshold
     * @param evaluationPeriods The number of periods to evaluate
     * @param period The period in seconds
     * @param statistic The statistic to apply to the metric
     * @param dimensions Additional dimensions for the metric
     * @return The ARN of the created alarm
     */
    public String createAlarm(String alarmName, String metricName, double threshold,
                              ComparisonOperator comparisonOperator, int evaluationPeriods,
                              int period, Statistic statistic, Map<String, String> dimensions) {
        log.info("Creating alarm: {} for metric: {}", alarmName, metricName);

        PutMetricAlarmRequest request = new PutMetricAlarmRequest()
                .withAlarmName(alarmName)
                .withMetricName(metricName)
                .withNamespace("BASF/JavaGIS")
                .withThreshold(threshold)
                .withComparisonOperator(comparisonOperator)
                .withEvaluationPeriods(evaluationPeriods)
                .withPeriod(period)
                .withStatistic(statistic)
                .withActionsEnabled(true);

        // Add application dimension
        Dimension applicationDimension = new Dimension()
                .withName("Application")
                .withValue(applicationName);
        request.withDimensions(applicationDimension);

        // Add custom dimensions
        if (dimensions != null) {
            for (Map.Entry<String, String> entry : dimensions.entrySet()) {
                Dimension dimension = new Dimension()
                        .withName(entry.getKey())
                        .withValue(entry.getValue());
                request.withDimensions(dimension);
            }
        }

        try {
            amazonCloudWatch.putMetricAlarm(request);
            log.info("Successfully created alarm: {}", alarmName);

            // Get the alarm ARN
            DescribeAlarmsRequest describeRequest = new DescribeAlarmsRequest()
                    .withAlarmNames(alarmName);
            DescribeAlarmsResult result = amazonCloudWatch.describeAlarms(describeRequest);

            if (!result.getMetricAlarms().isEmpty()) {
                return result.getMetricAlarms().get(0).getAlarmArn();
            }

            return null;
        } catch (Exception e) {
            log.error("Error creating alarm: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete a CloudWatch alarm.
     *
     * @param alarmName The name of the alarm to delete
     */
    public void deleteAlarm(String alarmName) {
        log.info("Deleting alarm: {}", alarmName);

        DeleteAlarmsRequest request = new DeleteAlarmsRequest()
                .withAlarmNames(alarmName);

        try {
            amazonCloudWatch.deleteAlarms(request);
            log.info("Successfully deleted alarm: {}", alarmName);
        } catch (Exception e) {
            log.error("Error deleting alarm: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Monitor API request latency.
     *
     * @param endpoint The API endpoint
     * @param latencyMs The latency in milliseconds
     */
    public void monitorApiLatency(String endpoint, long latencyMs) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("Endpoint", endpoint);

        publishMetric("ApiLatency", latencyMs, StandardUnit.Milliseconds, dimensions);
    }

    /**
     * Monitor API request count.
     *
     * @param endpoint The API endpoint
     * @param statusCode The HTTP status code
     */
    public void monitorApiRequestCount(String endpoint, int statusCode) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("Endpoint", endpoint);
        dimensions.put("StatusCode", String.valueOf(statusCode));

        publishMetric("ApiRequestCount", 1, StandardUnit.Count, dimensions);
    }

    /**
     * Monitor database query latency.
     *
     * @param queryType The type of query
     * @param latencyMs The latency in milliseconds
     */
    public void monitorDatabaseLatency(String queryType, long latencyMs) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("QueryType", queryType);

        publishMetric("DatabaseLatency", latencyMs, StandardUnit.Milliseconds, dimensions);
    }

    /**
     * Monitor AI workflow execution time.
     *
     * @param workflowType The type of workflow
     * @param executionTimeMs The execution time in milliseconds
     */
    public void monitorAiWorkflowExecution(String workflowType, long executionTimeMs) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("WorkflowType", workflowType);

        publishMetric("AiWorkflowExecutionTime", executionTimeMs, StandardUnit.Milliseconds, dimensions);
    }

    /**
     * Monitor geospatial operation performance.
     *
     * @param operationType The type of geospatial operation
     * @param executionTimeMs The execution time in milliseconds
     */
    public void monitorGeospatialOperation(String operationType, long executionTimeMs) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("OperationType", operationType);

        publishMetric("GeospatialOperationTime", executionTimeMs, StandardUnit.Milliseconds, dimensions);
    }

    /**
     * Monitor storage usage.
     *
     * @param storageType The type of storage (e.g., S3, Database)
     * @param sizeBytes The size in bytes
     */
    public void monitorStorageUsage(String storageType, long sizeBytes) {
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("StorageType", storageType);

        publishMetric("StorageUsage", sizeBytes, StandardUnit.Bytes, dimensions);
    }

    public void monitorGeospatialProcessing(String create, String shapefile, long processingTime) {
    }
}
