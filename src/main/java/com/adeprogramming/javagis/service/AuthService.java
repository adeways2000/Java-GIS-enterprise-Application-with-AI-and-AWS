package com.adeprogramming.javagis.service;

import com.adeprogramming.javagis.domain.security.User;
import com.adeprogramming.javagis.dto.AuthRequestDto;
import com.adeprogramming.javagis.dto.AuthResponseDto;
import com.adeprogramming.javagis.dto.UserDto;
import com.adeprogramming.javagis.exception.ResourceNotFoundException;
import com.adeprogramming.javagis.repository.UserRepository;
import com.adeprogramming.javagis.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for authentication and user management.
 * Provides methods for login, registration, and user management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate a user and generate JWT tokens.
     *
     * @param authRequest The authentication request
     * @return The authentication response with tokens
     * @throws BadCredentialsException if authentication fails
     */
    public AuthResponseDto login(AuthRequestDto authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
            String accessToken = jwtTokenUtil.generateToken(user);
            String refreshToken = jwtTokenUtil.generateRefreshToken(user);

            return new AuthResponseDto(
                    user.getId(),
                    user.getUsername(),
                    accessToken,
                    refreshToken
            );
        } catch (BadCredentialsException e) {
            log.error("Authentication failed for user: {}", authRequest.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    /**
     * Register a new user.
     *
     * @param userDto The user data
     * @return The registered user
     */
    @Transactional
    public UserDto register(UserDto userDto) {
        // Check if username already exists
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + userDto.getUsername());
        }

        // Check if email already exists
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + userDto.getEmail());
        }

        // Create new user
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEnabled(true);

        // Save user
        User savedUser = userRepository.save(user);

        return convertToDto(savedUser);
    }

    /**
     * Refresh an access token using a refresh token.
     *
     * @param refreshToken The refresh token
     * @return The new authentication response with tokens
     */
    public AuthResponseDto refreshToken(String refreshToken) {
        String username = jwtTokenUtil.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        if (jwtTokenUtil.validateToken(refreshToken, user)) {
            String accessToken = jwtTokenUtil.generateToken(user);
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(user);

            return new AuthResponseDto(
                    user.getId(),
                    user.getUsername(),
                    accessToken,
                    newRefreshToken
            );
        } else {
            throw new BadCredentialsException("Invalid refresh token");
        }
    }

    /**
     * Find all users.
     *
     * @return List of all users
     */
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find a user by ID.
     *
     * @param id The ID of the user
     * @return The user DTO
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public UserDto findById(UUID id) {
        return userRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Update a user.
     *
     * @param id The ID of the user to update
     * @param userDto The updated user data
     * @return The updated user
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional
    public UserDto update(UUID id, UserDto userDto) {
        User user = (User) userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update fields
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }

        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        // Save the updated entity
        User updatedUser = userRepository.save(user);

        return convertToDto(updatedUser);
    }

    /**
     * Delete a user.
     *
     * @param id The ID of the user to delete
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
    }

    /**
     * Convert a User entity to a DTO.
     *
     * @param user The entity to convert
     * @return The DTO
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEnabled(user.isEnabled());

        // Add roles
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}

