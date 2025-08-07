package com.adeprogramming.javagis.service.aws;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Service for interacting with AWS Lambda.
 * Provides methods for invoking Lambda functions and processing results.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LambdaService {

    private final AWSLambda awsLambda;

    /**
     * Invoke a Lambda function with a JSON payload.
     *
     * @param functionName The name of the Lambda function
     * @param payload The JSON payload to send to the function
     * @return The response from the Lambda function
     * @throws Exception if there is an error invoking the function
     */
    public String invokeLambdaFunction(String functionName, String payload) throws Exception {
        log.info("Invoking Lambda function: {} with payload: {}", functionName, payload);

        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName(functionName)
                .withPayload(payload);

        InvokeResult invokeResult = awsLambda.invoke(invokeRequest);

        // Check for errors
        if (invokeResult.getFunctionError() != null) {
            String errorType = invokeResult.getFunctionError();
            String errorMessage = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
            log.error("Lambda function error: {} - {}", errorType, errorMessage);
            throw new Exception("Lambda function error: " + errorType + " - " + errorMessage);
        }

        // Process successful result
        ByteBuffer resultPayload = invokeResult.getPayload();
        String result = new String(resultPayload.array(), StandardCharsets.UTF_8);
        log.info("Lambda function response: {}", result);

        return result;
    }

    /**
     * Invoke a Lambda function asynchronously with a JSON payload.
     *
     * @param functionName The name of the Lambda function
     * @param payload The JSON payload to send to the function
     * @throws Exception if there is an error invoking the function
     */
    public void invokeLambdaFunctionAsync(String functionName, String payload) throws Exception {
        log.info("Invoking Lambda function asynchronously: {} with payload: {}", functionName, payload);

        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName(functionName)
                .withInvocationType("Event") // Asynchronous invocation
                .withPayload(payload);

        awsLambda.invoke(invokeRequest);

        log.info("Asynchronous Lambda function invocation initiated");
    }

    /**
     * Invoke a Lambda function with binary data.
     *
     * @param functionName The name of the Lambda function
     * @param data The binary data to send to the function
     * @return The response from the Lambda function as a byte array
     * @throws Exception if there is an error invoking the function
     */
    public byte[] invokeLambdaFunctionWithBinaryData(String functionName, byte[] data) throws Exception {
        log.info("Invoking Lambda function with binary data: {}", functionName);

        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName(functionName)
                .withPayload(ByteBuffer.wrap(data));

        InvokeResult invokeResult = awsLambda.invoke(invokeRequest);

        // Check for errors
        if (invokeResult.getFunctionError() != null) {
            String errorType = invokeResult.getFunctionError();
            String errorMessage = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
            log.error("Lambda function error: {} - {}", errorType, errorMessage);
            throw new Exception("Lambda function error: " + errorType + " - " + errorMessage);
        }

        // Process successful result
        ByteBuffer resultPayload = invokeResult.getPayload();
        byte[] result = resultPayload.array();
        log.info("Lambda function response received, size: {} bytes", result.length);

        return result;
    }

    /**
     * Invoke a Lambda function with Base64-encoded data.
     *
     * @param functionName The name of the Lambda function
     * @param data The data to encode and send to the function
     * @return The decoded response from the Lambda function
     * @throws Exception if there is an error invoking the function
     */
    public byte[] invokeLambdaFunctionWithBase64Data(String functionName, byte[] data) throws Exception {
        log.info("Invoking Lambda function with Base64 data: {}", functionName);

        // Encode data as Base64
        String base64Data = Base64.getEncoder().encodeToString(data);
        String payload = "{\"data\":\"" + base64Data + "\"}";

        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName(functionName)
                .withPayload(payload);

        InvokeResult invokeResult = awsLambda.invoke(invokeRequest);

        // Check for errors
        if (invokeResult.getFunctionError() != null) {
            String errorType = invokeResult.getFunctionError();
            String errorMessage = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
            log.error("Lambda function error: {} - {}", errorType, errorMessage);
            throw new Exception("Lambda function error: " + errorType + " - " + errorMessage);
        }

        // Process successful result
        ByteBuffer resultPayload = invokeResult.getPayload();
        String resultString = new String(resultPayload.array(), StandardCharsets.UTF_8);

        // Extract Base64 data from response (assuming response format: {"data":"base64string"})
        // This is a simplified example; actual parsing would depend on the response format
        String base64Result = resultString.substring(resultString.indexOf("\"data\":\"") + 8, resultString.lastIndexOf("\""));
        byte[] result = Base64.getDecoder().decode(base64Result);

        log.info("Lambda function response received and decoded, size: {} bytes", result.length);

        return result;
    }

    public Map<String, Object> invokeFunction(String functionName, Map<String, Object> payload) {
        return payload;
    }
}
