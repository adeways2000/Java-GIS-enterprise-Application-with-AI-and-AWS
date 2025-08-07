package com.adeprogramming.javagis.controller;

import com.adeprogramming.javagis.dto.AuthRequestDto;
import com.adeprogramming.javagis.dto.AuthResponseDto;
import com.adeprogramming.javagis.dto.UserDto;
import com.adeprogramming.javagis.service.AuthService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for authentication and user management.
 * Provides endpoints for login, registration, and user management.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication API", description = "API for authentication and user management")
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticate a user and generate JWT tokens.
     *
     * @param authRequest The authentication request
     * @return The authentication response with tokens
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate a user and generate JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed")
    })
    public ResponseEntity<AuthResponseDto> login(
            @Parameter(description = "Authentication request") @RequestBody @Valid AuthRequestDto authRequest) {
        AuthResponseDto authResponse = authService.login(authRequest);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Register a new user.
     *
     * @param userDto The user data
     * @return The registered user
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<UserDto> register(
            @Parameter(description = "User data") @RequestBody @Valid UserDto userDto) {
        UserDto registeredUser = authService.register(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    /**
     * Refresh an access token using a refresh token.
     *
     * @param refreshToken The refresh token
     * @return The new authentication response with tokens
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh an access token using a refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public ResponseEntity<AuthResponseDto> refreshToken(
            @Parameter(description = "Refresh token") @RequestParam String refreshToken) {
        AuthResponseDto authResponse = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Get all users.
     *
     * @return List of all users
     */
    @GetMapping("/users")
    @Operation(summary = "Get all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all users",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = authService.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * Get a user by ID.
     *
     * @param id The ID of the user
     * @return The user
     */
    @GetMapping("/users/{id}")
    @Operation(summary = "Get a user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "ID of the user to retrieve") @PathVariable UUID id) {
        UserDto user = authService.findById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Update a user.
     *
     * @param id The ID of the user to update
     * @param userDto The updated user data
     * @return The updated user
     */
    @PutMapping("/users/{id}")
    @Operation(summary = "Update a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "ID of the user to update") @PathVariable UUID id,
            @Parameter(description = "Updated user data") @RequestBody @Valid UserDto userDto) {
        UserDto updatedUser = authService.update(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete a user.
     *
     * @param id The ID of the user to delete
     * @return No content
     */
    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable UUID id) {
        authService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

