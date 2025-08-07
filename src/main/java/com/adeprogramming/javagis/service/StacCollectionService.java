package com.adeprogramming.javagis.service;

import com.adeprogramming.javagis.domain.stac.StacCollection;
import com.adeprogramming.javagis.domain.stac.StacItem;
import com.adeprogramming.javagis.domain.stac.StacLink;
import com.adeprogramming.javagis.dto.StacCollectionDto;
import com.adeprogramming.javagis.exception.ResourceNotFoundException;
import com.adeprogramming.javagis.repository.StacCollectionRepository;
import com.adeprogramming.javagis.repository.StacItemRepository;
import com.adeprogramming.javagis.util.StacUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing STAC collections.
 * Provides methods for CRUD operations and search on STAC collections.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StacCollectionService {

    private final StacCollectionRepository stacCollectionRepository;
    private final StacItemRepository stacItemRepository;
    private final StacUtil stacUtil;

    /**
     * Find all STAC collections.
     *
     * @return List of all STAC collections
     */
    @Transactional(readOnly = true)
    public List<StacCollectionDto> findAll() {
        return stacCollectionRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find a STAC collection by ID.
     *
     * @param id The ID of the STAC collection
     * @return The STAC collection DTO
     * @throws ResourceNotFoundException if the STAC collection is not found
     */
    @Transactional(readOnly = true)
    public StacCollectionDto findById(UUID id) {
        return stacCollectionRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("STAC collection not found with id: " + id));
    }

    /**
     * Find a STAC collection by STAC ID.
     *
     * @param stacId The STAC ID of the collection
     * @return The STAC collection DTO
     * @throws ResourceNotFoundException if the STAC collection is not found
     */
    @Transactional(readOnly = true)
    public StacCollectionDto findByStacId(String stacId) {
        return stacCollectionRepository.findByStacId(stacId)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("STAC collection not found with STAC ID: " + stacId));
    }

    /**
     * Find STAC collections by title containing the given text.
     *
     * @param title The title to search for
     * @return List of matching STAC collections
     */
    @Transactional(readOnly = true)
    public List<StacCollectionDto> findByTitle(String title) {
        return stacCollectionRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find STAC collections with temporal extent overlapping the given time range.
     *
     * @param startDate The start date
     * @param endDate The end date
     * @return List of matching STAC collections
     */
    @Transactional(readOnly = true)
    public List<StacCollectionDto> findByTemporalExtent(LocalDateTime startDate, LocalDateTime endDate) {
        return stacCollectionRepository.findByTemporalExtentOverlapping(startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Find STAC collections that intersect with the given geometry.
     *
     * @param geometry The geometry to check for intersection
     * @return List of matching STAC collections
     */
    @Transactional(readOnly = true)
    public List<StacCollectionDto> findByIntersectingGeometry(Geometry geometry) {
        return stacCollectionRepository.findByIntersectingGeometry(geometry).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a new STAC collection.
     *
     * @param dto The STAC collection DTO
     * @return The created STAC collection DTO
     */
    @Transactional
    public StacCollectionDto create(StacCollectionDto dto) {
        // Generate STAC ID if not provided
        if (dto.getStacId() == null || dto.getStacId().isEmpty()) {
            dto.setStacId(stacUtil.generateStacId("collection"));
        }

        // Check if STAC ID already exists
        if (stacCollectionRepository.findByStacId(dto.getStacId()).isPresent()) {
            throw new IllegalArgumentException("STAC collection with STAC ID already exists: " + dto.getStacId());
        }

        // Create the STAC collection entity
        StacCollection stacCollection = new StacCollection();
        stacCollection.setStacId(dto.getStacId());
        stacCollection.setTitle(dto.getTitle());
        stacCollection.setDescription(dto.getDescription());
        stacCollection.setLicense(dto.getLicense());
        stacCollection.setStartDatetime(dto.getStartDatetime());
        stacCollection.setEndDatetime(dto.getEndDatetime());
        stacCollection.setSpatialExtent((Polygon) dto.getSpatialExtent());

        // Set keywords
        if (dto.getKeywords() != null) {
            stacCollection.setKeywords(dto.getKeywords());
        }

        // Set providers
        if (dto.getProviders() != null) {
            List<StacCollection.StacProvider> providers = dto.getProviders().stream()
                    .map(p -> new StacCollection.StacProvider(
                            p.getName(),
                            p.getDescription(),
                            p.getRoles(),
                            p.getUrl()
                    ))
                    .collect(Collectors.toList());
            stacCollection.setProviders(providers);
        }

        // Set properties
        if (dto.getProperties() != null) {
            stacCollection.setProperties(dto.getProperties());
        }

        // Add links
        if (dto.getLinks() != null) {
            List<StacLink> links = dto.getLinks().stream()
                    .map(l -> {
                        StacLink link = new StacLink();
                        link.setRel(l.getRel());
                        link.setHref(l.getHref());
                        link.setType(l.getType());
                        link.setTitle(l.getTitle());
                        return link;
                    })
                    .collect(Collectors.toList());
            stacCollection.setLinks(links);
        }

        // Save the entity
        StacCollection savedCollection = stacCollectionRepository.save(stacCollection);

        return convertToDto(savedCollection);
    }

    /**
     * Update an existing STAC collection.
     *
     * @param id The ID of the STAC collection to update
     * @param dto The updated STAC collection DTO
     * @return The updated STAC collection DTO
     * @throws ResourceNotFoundException if the STAC collection is not found
     */
    @Transactional
    public StacCollectionDto update(UUID id, StacCollectionDto dto) {
        StacCollection stacCollection = stacCollectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("STAC collection not found with id: " + id));

        // Update fields
        stacCollection.setTitle(dto.getTitle());
        stacCollection.setDescription(dto.getDescription());
        stacCollection.setLicense(dto.getLicense());
        stacCollection.setStartDatetime(dto.getStartDatetime());
        stacCollection.setEndDatetime(dto.getEndDatetime());

        // Update spatial extent if provided
        if (dto.getSpatialExtent() != null) {
            stacCollection.setSpatialExtent((Polygon) dto.getSpatialExtent());
        }

        // Update keywords if provided
        if (dto.getKeywords() != null) {
            stacCollection.setKeywords(dto.getKeywords());
        }

        // Update providers if provided
        if (dto.getProviders() != null) {
            List<StacCollection.StacProvider> providers = dto.getProviders().stream()
                    .map(p -> new StacCollection.StacProvider(
                            p.getName(),
                            p.getDescription(),
                            p.getRoles(),
                            p.getUrl()
                    ))
                    .collect(Collectors.toList());
            stacCollection.setProviders(providers);
        }

        // Update properties if provided
        if (dto.getProperties() != null) {
            stacCollection.setProperties(dto.getProperties());
        }

        // Update links if provided
        if (dto.getLinks() != null) {
            // Remove existing links
            stacCollection.getLinks().clear();

            // Add new links
            List<StacLink> links = dto.getLinks().stream()
                    .map(l -> {
                        StacLink link = new StacLink();
                        link.setRel(l.getRel());
                        link.setHref(l.getHref());
                        link.setType(l.getType());
                        link.setTitle(l.getTitle());
                        return link;
                    })
                    .collect(Collectors.toList());
            stacCollection.getLinks().addAll(links);
        }

        // Save the updated entity
        StacCollection updatedCollection = stacCollectionRepository.save(stacCollection);

        return convertToDto(updatedCollection);
    }

    /**
     * Delete a STAC collection by ID.
     *
     * @param id The ID of the STAC collection to delete
     * @throws ResourceNotFoundException if the STAC collection is not found
     */
    @Transactional
    public void delete(UUID id) {
        StacCollection stacCollection = stacCollectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("STAC collection not found with id: " + id));

        // Check if collection has items
        List<StacItem> items = stacItemRepository.findByCollectionId(id);
        if (!items.isEmpty()) {
            throw new IllegalStateException("Cannot delete STAC collection with items. Delete items first.");
        }

        // Delete the entity
        stacCollectionRepository.delete(stacCollection);
    }

    /**
     * Get STAC collection metadata in STAC format.
     *
     * @param id The ID of the STAC collection
     * @return The STAC collection metadata as a Map
     * @throws ResourceNotFoundException if the STAC collection is not found
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStacMetadata(UUID id) {
        StacCollection stacCollection = stacCollectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("STAC collection not found with id: " + id));

        // Create extent
        double[] bbox = new double[4];
        if (stacCollection.getSpatialExtent() != null) {
            bbox[0] = stacCollection.getSpatialExtent().getEnvelopeInternal().getMinX(); // west
            bbox[1] = stacCollection.getSpatialExtent().getEnvelopeInternal().getMinY(); // south
            bbox[2] = stacCollection.getSpatialExtent().getEnvelopeInternal().getMaxX(); // east
            bbox[3] = stacCollection.getSpatialExtent().getEnvelopeInternal().getMaxY(); // north
        }

        Map<String, Object> extent = stacUtil.createStacExtent(
                bbox,
                stacCollection.getStartDatetime(),
                stacCollection.getEndDatetime()
        );

        // Create links
        List<Map<String, String>> links = stacCollection.getLinks().stream()
                .map(link -> stacUtil.createStacLink(
                        link.getHref(),
                        link.getRel(),
                        link.getType(),
                        link.getTitle()
                ))
                .collect(Collectors.toList());

        // Create providers
        List<Map<String, String>> providers = stacCollection.getProviders().stream()
                .map(provider -> {
                    Map<String, String> p = Map.of(
                            "name", provider.getName(),
                            "roles", provider.getRoles()
                    );

                    if (provider.getDescription() != null) {
                        p.put("description", provider.getDescription());
                    }

                    if (provider.getUrl() != null) {
                        p.put("url", provider.getUrl());
                    }

                    return p;
                })
                .collect(Collectors.toList());

        // Create STAC collection metadata
        return stacUtil.createStacCollectionMetadata(
                stacCollection.getStacId(),
                stacCollection.getTitle(),
                stacCollection.getDescription(),
                stacCollection.getLicense(),
                extent,
                stacCollection.getKeywords(),
                providers,
                links
        );
    }

    /**
     * Convert a StacCollection entity to a DTO.
     *
     * @param stacCollection The entity to convert
     * @return The DTO
     */
    private StacCollectionDto convertToDto(StacCollection stacCollection) {
        StacCollectionDto dto = new StacCollectionDto();
        dto.setId(stacCollection.getId());
        dto.setStacId(stacCollection.getStacId());
        dto.setTitle(stacCollection.getTitle());
        dto.setDescription(stacCollection.getDescription());
        dto.setLicense(stacCollection.getLicense());
        dto.setStartDatetime(stacCollection.getStartDatetime());
        dto.setEndDatetime(stacCollection.getEndDatetime());
        dto.setSpatialExtent(stacCollection.getSpatialExtent());

        if (stacCollection.getKeywords() != null) {
            dto.setKeywords(stacCollection.getKeywords());
        }

        if (stacCollection.getProviders() != null) {
            List<StacCollectionDto.StacProviderDto> providers = stacCollection.getProviders().stream()
                    .map(p -> new StacCollectionDto.StacProviderDto(
                            p.getName(),
                            p.getDescription(),
                            p.getRoles(),
                            p.getUrl()
                    ))
                    .collect(Collectors.toList());
            dto.setProviders(providers);
        }

        if (stacCollection.getProperties() != null) {
            dto.setProperties(stacCollection.getProperties());
        }

        if (stacCollection.getLinks() != null) {
            List<StacCollectionDto.StacLinkDto> links = stacCollection.getLinks().stream()
                    .map(l -> new StacCollectionDto.StacLinkDto(
                            l.getRel(),
                            l.getHref(),
                            l.getType(),
                            l.getTitle()
                    ))
                    .collect(Collectors.toList());
            dto.setLinks(links);
        }

        // Count items
        long itemCount = stacItemRepository.countByCollectionId(stacCollection.getId());
        dto.setItemCount(itemCount);

        return dto;
    }
}

