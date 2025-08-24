package com.adeprogramming.javagis.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an error processing geospatial data.
 * Results in HTTP 400 Bad Request response.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
public class GeospatialProcessingException extends RuntimeException {

    private final String resourceType;
    private final String operation;

    public GeospatialProcessingException(String message, String resourceType, String operation) {
        super(message);
        this.resourceType = resourceType;
        this.operation = operation;
    }

    public GeospatialProcessingException(String message, String resourceType, String operation, Throwable cause) {
        super(message, cause);
        this.resourceType = resourceType;
        this.operation = operation;
    }
}
