package com.adeprogramming.javagis.util;

import lombok.extern.slf4j.Slf4j;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for GeoTools operations.
 * Provides methods for working with shapefiles and other geospatial data.
 */
@Component
@Slf4j
public class GeoToolsUtil {

    /**
     * Read a shapefile and extract its features.
     *
     * @param shapefilePath The path to the shapefile
     * @return A list of features
     * @throws IOException if there is an error reading the shapefile
     */
    public List<Map<String, Object>> readShapefile(String shapefilePath) throws IOException {
        File file = new File(shapefilePath);

        // Use FileDataStoreFinder instead of direct ShapefileDataStore
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(file);
        if (dataStore == null) {
            throw new IOException("Could not create data store for file: " + shapefilePath);
        }

        try {
            String typeName = dataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
            SimpleFeatureCollection featureCollection = featureSource.getFeatures();

            List<Map<String, Object>> features = new ArrayList<>();

            try (SimpleFeatureIterator iterator = featureCollection.features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    Map<String, Object> attributes = new HashMap<>();

                    // Extract attributes
                    feature.getProperties().forEach(property -> {
                        if (property.getValue() != null) {
                            attributes.put(property.getName().getLocalPart(), property.getValue());
                        }
                    });

                    features.add(attributes);
                }
            }

            return features;
        } finally {
            dataStore.dispose();
        }
    }

    /**
     * Transform a geometry from one coordinate reference system to another.
     *
     * @param geometry The geometry to transform
     * @param sourceCRS The source coordinate reference system
     * @param targetCRS The target coordinate reference system
     * @return The transformed geometry
     * @throws FactoryException if there is an error creating the transform
     * @throws TransformException if there is an error transforming the geometry
     */
    public Geometry transformGeometry(Geometry geometry, CoordinateReferenceSystem sourceCRS,
                                      CoordinateReferenceSystem targetCRS) throws FactoryException, TransformException {
        if (CRS.equalsIgnoreMetadata(sourceCRS, targetCRS)) {
            return geometry;
        }

        // Use the correct method signature for findMathTransform
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
        return JTS.transform(geometry, transform);
    }

    /**
     * Create a SimpleFeatureType for a shapefile.
     *
     * @param typeName The name of the feature type
     * @param attributes The attributes of the feature type
     * @param geometryType The geometry type
     * @param crs The coordinate reference system
     * @return The SimpleFeatureType
     */
    public SimpleFeatureType createFeatureType(String typeName, Map<String, Class<?>> attributes,
                                               Class<? extends Geometry> geometryType,
                                               CoordinateReferenceSystem crs) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(typeName);
        builder.setCRS(crs);

        // Add geometry attribute
        builder.add("the_geom", geometryType);

        // Add other attributes
        attributes.forEach(builder::add);

        return builder.buildFeatureType();
    }

    /**
     * Get a coordinate reference system by EPSG code.
     *
     * @param epsgCode The EPSG code
     * @return The coordinate reference system
     * @throws FactoryException if there is an error creating the CRS
     */
    public CoordinateReferenceSystem getCRS(String epsgCode) throws FactoryException {
        return CRS.decode(epsgCode, true);
    }

    /**
     * Calculate the area of a geometry in square meters.
     *
     * @param geometry The geometry
     * @param crs The coordinate reference system of the geometry
     * @return The area in square meters
     * @throws FactoryException if there is an error creating the transform
     * @throws TransformException if there is an error transforming the geometry
     */
    public double calculateAreaInSquareMeters(Geometry geometry, CoordinateReferenceSystem crs)
            throws FactoryException, TransformException {
        // Check if CRS is already projected
        if (isProjectedCRS(crs)) {
            return geometry.getArea();
        }

        // Transform to a projected CRS if necessary
        CoordinateReferenceSystem utmCRS = findUTMZone(geometry, crs);
        Geometry projectedGeometry = transformGeometry(geometry, crs, utmCRS);
        return projectedGeometry.getArea();
    }

    /**
     * Check if a CRS is projected.
     *
     * @param crs The coordinate reference system
     * @return true if the CRS is projected, false otherwise
     */
    private boolean isProjectedCRS(CoordinateReferenceSystem crs) {
        try {
            // Simple check - if it's not geographic, assume it's projected
            return !CRS.getAxisOrder(crs).equals(CRS.getAxisOrder(CRS.decode("EPSG:4326")));
        } catch (Exception e) {
            // If we can't determine, assume it's geographic
            return false;
        }
    }

    /**
     * Find the appropriate UTM zone for a geometry.
     *
     * @param geometry The geometry
     * @param sourceCRS The source coordinate reference system
     * @return The UTM coordinate reference system
     * @throws FactoryException if there is an error creating the CRS
     * @throws TransformException if there is an error transforming the geometry
     */
    private CoordinateReferenceSystem findUTMZone(Geometry geometry, CoordinateReferenceSystem sourceCRS)
            throws FactoryException, TransformException {
        // Transform to WGS84 if necessary
        CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326", true);
        Geometry wgs84Geometry = geometry;

        if (!CRS.equalsIgnoreMetadata(sourceCRS, wgs84)) {
            wgs84Geometry = transformGeometry(geometry, sourceCRS, wgs84);
        }

        // Get centroid
        Geometry centroid = wgs84Geometry.getCentroid();
        double lon = centroid.getCoordinate().x;
        double lat = centroid.getCoordinate().y;

        // Calculate UTM zone
        int zone = (int) Math.floor((lon + 180) / 6) + 1;
        String epsg = lat > 0 ? "326" : "327"; // Northern or Southern hemisphere
        epsg += String.format("%02d", zone);

        return CRS.decode("EPSG:" + epsg, true);
    }

    /**
     * Get the bounds of a geometry in the specified CRS.
     *
     * @param geometry The geometry
     * @param crs The coordinate reference system
     * @return A map containing the bounds (minX, minY, maxX, maxY)
     */
    public Map<String, Double> getGeometryBounds(Geometry geometry, CoordinateReferenceSystem crs) {
        Map<String, Double> bounds = new HashMap<>();

        org.locationtech.jts.geom.Envelope envelope = geometry.getEnvelopeInternal();
        bounds.put("minX", envelope.getMinX());
        bounds.put("minY", envelope.getMinY());
        bounds.put("maxX", envelope.getMaxX());
        bounds.put("maxY", envelope.getMaxY());

        return bounds;
    }

    /**
     * Check if two geometries intersect.
     *
     * @param geometry1 The first geometry
     * @param geometry2 The second geometry
     * @return true if the geometries intersect, false otherwise
     */
    public boolean geometriesIntersect(Geometry geometry1, Geometry geometry2) {
        try {
            return geometry1.intersects(geometry2);
        } catch (Exception e) {
            log.warn("Error checking geometry intersection: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Calculate the distance between two geometries.
     *
     * @param geometry1 The first geometry
     * @param geometry2 The second geometry
     * @param crs The coordinate reference system
     * @return The distance between the geometries
     * @throws FactoryException if there is an error with the CRS
     * @throws TransformException if there is an error transforming geometries
     */
    public double calculateDistance(Geometry geometry1, Geometry geometry2, CoordinateReferenceSystem crs)
            throws FactoryException, TransformException {
        // If not projected, transform to appropriate UTM zone
        if (!isProjectedCRS(crs)) {
            CoordinateReferenceSystem utmCRS = findUTMZone(geometry1, crs);
            geometry1 = transformGeometry(geometry1, crs, utmCRS);
            geometry2 = transformGeometry(geometry2, crs, utmCRS);
        }

        return geometry1.distance(geometry2);
    }
}
