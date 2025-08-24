package com.adeprogramming.javagis.util;

import lombok.extern.slf4j.Slf4j;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for working with satellite imagery.
 * Provides methods for reading and processing satellite images.
 */
@Component
@Slf4j
public class SatelliteImageUtil {

    /**
     * Read a satellite image file and return a GridCoverage2D object.
     *
     * @param imagePath The path to the satellite image file
     * @return The GridCoverage2D object
     * @throws IOException if there is an error reading the image
     */
    public GridCoverage2D readSatelliteImage(String imagePath) throws IOException {
        File file = new File(imagePath);

        // Find the appropriate format
        AbstractGridFormat format = GridFormatFinder.findFormat(file);
        if (format == null) {
            throw new IOException("No suitable format found for file: " + imagePath);
        }

        // Read the coverage
        GridCoverage2DReader reader = format.getReader(file);
        if (reader == null) {
            throw new IOException("Failed to create reader for file: " + imagePath);
        }

        GridCoverage2D coverage = reader.read(null);
        return coverage;
    }

    /**
     * Extract metadata from a satellite image file.
     *
     * @param imagePath The path to the satellite image file
     * @return The extracted metadata
     * @throws IOException if there is an error reading the image
     */
    public Map<String, Object> extractMetadata(String imagePath) throws IOException {
        File file = new File(imagePath);

        // Find the appropriate format
        AbstractGridFormat format = GridFormatFinder.findFormat(file);
        if (format == null) {
            throw new IOException("No suitable format found for file: " + imagePath);
        }

        // Read the coverage
        GridCoverage2DReader reader = format.getReader(file);
        if (reader == null) {
            throw new IOException("Failed to create reader for file: " + imagePath);
        }

        // Extract metadata
        Map<String, Object> metadata = new HashMap<>();
        String[] metadataNames = reader.getMetadataNames();
        if (metadataNames != null) {
            for (String name : metadataNames) {
                metadata.put(name, reader.getMetadataValue(name));
            }
        }

        // Add coverage information
        try {
            GridCoverage2D coverage = reader.read(null);
            metadata.put("width", coverage.getRenderedImage().getWidth());
            metadata.put("height", coverage.getRenderedImage().getHeight());
            metadata.put("numBands", coverage.getNumSampleDimensions());
            metadata.put("crs", coverage.getCoordinateReferenceSystem().getName().toString());

            // Clean up
            coverage.dispose(true);
        } catch (Exception e) {
            log.warn("Error extracting coverage information: {}", e.getMessage());
        }

        // Clean up
        reader.dispose();

        return metadata;
    }

    /**
     * Extract the footprint (bounding polygon) from a satellite image.
     *
     * @param imagePath The path to the satellite image file
     * @return The footprint as a JTS Polygon
     * @throws IOException if there is an error reading the image
     */
    public Polygon extractFootprint(String imagePath) throws IOException {
        File file = new File(imagePath);

        // Use GeoTiff format explicitly for better handling of georeferenced data
        GeoTiffFormat format = new GeoTiffFormat();
        GridCoverage2DReader reader = format.getReader(file);

        if (reader == null) {
            throw new IOException("Failed to create GeoTIFF reader for file: " + imagePath);
        }

        try {
            GridCoverage2D coverage = reader.read(null);

            // Get the envelope
            ReferencedEnvelope envelope = new ReferencedEnvelope(ReferencedEnvelope.create(ReferencedEnvelope.reference(coverage.getEnvelope2D())));

            // Convert to JTS polygon
            Polygon footprint = JTS.toGeometry(envelope);

            // Clean up
            coverage.dispose(true);
            reader.dispose();

            return footprint;
        } catch (Exception e) {
            reader.dispose();
            throw new IOException("Error extracting footprint: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate cloud cover percentage in a satellite image.
     * This is a simplified implementation that would need to be replaced with
     * actual cloud detection algorithms in a production environment.
     *
     * @param coverage The GridCoverage2D object
     * @return The estimated cloud cover percentage
     */
    public double calculateCloudCoverPercentage(GridCoverage2D coverage) {
        // In a real implementation, this would use image processing techniques
        // to detect clouds based on spectral signatures, brightness thresholds, etc.
        // For now, we return a placeholder value
        log.info("Cloud cover calculation would require actual image processing algorithms");
        return 0.0; // Placeholder
    }

    /**
     * Generate a thumbnail for a satellite image.
     *
     * @param imagePath The path to the satellite image file
     * @param thumbnailPath The path to save the thumbnail
     * @param width The width of the thumbnail
     * @param height The height of the thumbnail
     * @throws IOException if there is an error processing the image
     */
    public void generateThumbnail(String imagePath, String thumbnailPath, int width, int height) throws IOException {
        // In a real implementation, this would use Java2D or another imaging library
        // to resize the image and save it as a thumbnail
        log.info("Thumbnail generation would require actual image processing code");
    }
}

