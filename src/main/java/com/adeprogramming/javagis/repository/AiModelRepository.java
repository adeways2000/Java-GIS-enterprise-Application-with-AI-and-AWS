package com.adeprogramming.javagis.repository;

import com.adeprogramming.javagis.domain.ai.AiModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AiModel entity.
 * Provides methods for accessing AI model data.
 */
@Repository
public interface AiModelRepository extends JpaRepository<AiModel, UUID> {

    /**
     * Find models by name containing the given text.
     *
     * @param name The name to search for
     * @return List of matching models
     */
    List<AiModel> findByNameContainingIgnoreCase(String name);

    /**
     * Find models by type.
     *
     * @param type The model type
     * @return List of matching models
     */
    List<AiModel> findByType(AiModel.ModelType type);

    /**
     * Find active models.
     *
     * @return List of active models
     */
    List<AiModel> findByIsActiveTrue();

    /**
     * Find models by version.
     *
     * @param version The version to search for
     * @return List of matching models
     */
    List<AiModel> findByVersion(String version);

    /**
     * Find models with accuracy greater than or equal to the given value.
     *
     * @param accuracy The minimum accuracy
     * @return List of matching models
     */
    List<AiModel> findByAccuracyGreaterThanEqual(Double accuracy);

    /**
     * Find models used in a specific workflow.
     *
     * @param workflowId The ID of the workflow
     * @return List of models used in the workflow
     */
    @Query("SELECT m FROM AiModel m JOIN m.workflows w WHERE w.id = :workflowId")
    List<AiModel> findByWorkflowId(@Param("workflowId") UUID workflowId);

    /**
     * Find the latest version of a model by name.
     *
     * @param name The name of the model
     * @return The latest version of the model, if found
     */
    @Query("SELECT m FROM AiModel m WHERE m.name = :name ORDER BY m.version DESC")
    List<AiModel> findLatestVersionByName(@Param("name") String name);
}
