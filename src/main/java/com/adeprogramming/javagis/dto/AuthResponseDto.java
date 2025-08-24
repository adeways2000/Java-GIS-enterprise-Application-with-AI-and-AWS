package com.adeprogramming.javagis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object for authentication responses.
 * Used for returning JWT tokens after successful authentication.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    private UUID userId;
    private String username;
    private String accessToken;
    private String refreshToken;
}
