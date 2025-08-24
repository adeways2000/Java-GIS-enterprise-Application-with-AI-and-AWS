# JavaGIS Enterprise Application - API Documentation

**Version**: 1.0  
**Base URL**: `https://api.javagis.com/api`  
**Authentication**: JWT Bearer Token  
**Content-Type**: `application/json`

---

## Table of Contents

1. [Authentication](#authentication)
2. [Satellite Images API](#satellite-images-api)
3. [Shapefiles API](#shapefiles-api)
4. [STAC Catalog API](#stac-catalog-api)
5. [AI Workflows API](#ai-workflows-api)
6. [User Management API](#user-management-api)
7. [AWS Services API](#aws-services-api)
8. [Error Handling](#error-handling)
9. [Rate Limiting](#rate-limiting)
10. [Examples](#examples)

---

## Authentication

### POST /auth/login
Authenticate user and obtain JWT token.

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "string",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@javagis.com",
    "roles": ["ADMIN", "ANALYST"]
  }
}
```

### POST /auth/refresh
Refresh JWT token using refresh token.

**Request Body:**
```json
{
  "refreshToken": "string"
}
```

### POST /auth/logout
Invalidate current session and tokens.

**Headers:**
```
Authorization: Bearer <token>
```

---

## Satellite Images API

### GET /satellite-images
Retrieve paginated list of satellite images with filtering options.

**Query Parameters:**
- `page` (integer): Page number (default: 0)
- `size` (integer): Page size (default: 20)
- `startDate` (string): Start date filter (ISO 8601)
- `endDate` (string): End date filter (ISO 8601)
- `platform` (string): Satellite platform filter
- `cloudCover` (number): Maximum cloud coverage percentage
- `bbox` (string): Bounding box filter (minX,minY,maxX,maxY)

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "filename": "S2A_MSIL2A_20240615T103031_N0510_R108_T32UMA_20240615T134849.SAFE",
      "platform": "Sentinel-2A",
      "acquisitionDate": "2024-06-15T10:30:31Z",
      "cloudCoveragePercentage": 15.2,
      "spatialExtent": {
        "type": "Polygon",
        "coordinates": [[[8.4, 49.4], [8.5, 49.4], [8.5, 49.5], [8.4, 49.5], [8.4, 49.4]]]
      },
      "processingLevel": "L2A",
      "fileSize": 1024000000,
      "downloadUrl": "/api/satellite-images/1/download"
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "size": 20,
  "number": 0
}
```

### POST /satellite-images
Upload and process new satellite image.

**Request:** Multipart form data
- `file`: Satellite image file
- `metadata`: JSON metadata object

**Response:**
```json
{
  "id": 1,
  "status": "PROCESSING",
  "message": "Image uploaded successfully and processing started",
  "processingId": "proc_123456789"
}
```

### GET /satellite-images/{id}
Retrieve detailed information about specific satellite image.

**Response:**
```json
{
  "id": 1,
  "filename": "S2A_MSIL2A_20240615T103031_N0510_R108_T32UMA_20240615T134849.SAFE",
  "platform": "Sentinel-2A",
  "acquisitionDate": "2024-06-15T10:30:31Z",
  "cloudCoveragePercentage": 15.2,
  "spatialExtent": {
    "type": "Polygon",
    "coordinates": [[[8.4, 49.4], [8.5, 49.4], [8.5, 49.5], [8.4, 49.5], [8.4, 49.4]]]
  },
  "processingLevel": "L2A",
  "fileSize": 1024000000,
  "bands": [
    {"name": "B02", "description": "Blue", "wavelength": "490nm"},
    {"name": "B03", "description": "Green", "wavelength": "560nm"},
    {"name": "B04", "description": "Red", "wavelength": "665nm"},
    {"name": "B08", "description": "NIR", "wavelength": "842nm"}
  ],
  "metadata": {
    "sensor": "MSI",
    "orbitNumber": 108,
    "tileId": "T32UMA"
  },
  "createdAt": "2024-06-15T12:00:00Z",
  "updatedAt": "2024-06-15T12:30:00Z"
}
```

### PUT /satellite-images/{id}
Update satellite image metadata.

**Request Body:**
```json
{
  "description": "Updated description",
  "tags": ["environmental", "monitoring"],
  "metadata": {
    "customField": "value"
  }
}
```

### DELETE /satellite-images/{id}
Remove satellite image from catalog and storage.

**Response:**
```json
{
  "message": "Satellite image deleted successfully"
}
```

### GET /satellite-images/{id}/download
Download satellite image file.

**Response:** Binary file download

### POST /satellite-images/{id}/analyze
Trigger analysis workflow for satellite image.

**Request Body:**
```json
{
  "analysisType": "NDVI",
  "parameters": {
    "outputFormat": "GeoTIFF",
    "noDataValue": -9999
  }
}
```

---

## Shapefiles API

### GET /shapefiles
Retrieve list of available shapefiles.

**Query Parameters:**
- `page` (integer): Page number
- `size` (integer): Page size
- `name` (string): Name filter
- `category` (string): Category filter
- `bbox` (string): Spatial filter

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "BASF_Ludwigshafen_Boundaries",
      "description": "Facility boundaries for BASF Ludwigshafen site",
      "category": "FACILITY_BOUNDARIES",
      "featureCount": 25,
      "spatialExtent": {
        "type": "Polygon",
        "coordinates": [[[8.4, 49.4], [8.5, 49.4], [8.5, 49.5], [8.4, 49.5], [8.4, 49.4]]]
      },
      "crs": "EPSG:4326",
      "createdAt": "2024-06-15T10:00:00Z"
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

### POST /shapefiles
Import new shapefile.

**Request:** Multipart form data
- `shpFile`: .shp file
- `shxFile`: .shx file
- `dbfFile`: .dbf file
- `prjFile`: .prj file (optional)
- `metadata`: JSON metadata

### GET /shapefiles/{id}/features
Retrieve features from shapefile with optional spatial filtering.

**Query Parameters:**
- `bbox` (string): Bounding box filter
- `cql_filter` (string): CQL filter expression
- `limit` (integer): Maximum features to return

**Response:**
```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "id": 1,
      "geometry": {
        "type": "Polygon",
        "coordinates": [[[8.4, 49.4], [8.45, 49.4], [8.45, 49.45], [8.4, 49.45], [8.4, 49.4]]]
      },
      "properties": {
        "name": "Production Unit A",
        "type": "Chemical Plant",
        "capacity": 50000,
        "status": "Active"
      }
    }
  ]
}
```

---

## STAC Catalog API

### GET /stac/collections
List available STAC collections.

**Response:**
```json
{
  "collections": [
    {
      "id": "environmental-monitoring",
      "title": "Environmental Monitoring Collection",
      "description": "Satellite imagery and sensor data for environmental monitoring",
      "extent": {
        "spatial": {
          "bbox": [[8.0, 49.0, 9.0, 50.0]]
        },
        "temporal": {
          "interval": [["2024-01-01T00:00:00Z", "2024-12-31T23:59:59Z"]]
        }
      },
      "license": "proprietary",
      "providers": [
        {
          "name": "BASF GmbH",
          "roles": ["producer", "processor"]
        }
      ]
    }
  ]
}
```

### GET /stac/search
Search for STAC items across collections.

**Query Parameters:**
- `collections` (array): Collection IDs to search
- `bbox` (array): Bounding box [minX, minY, maxX, maxY]
- `datetime` (string): Temporal filter (ISO 8601)
- `limit` (integer): Maximum items to return

**Response:**
```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "stac_version": "1.0.0",
      "id": "S2A_20240615_T32UMA",
      "collection": "environmental-monitoring",
      "geometry": {
        "type": "Polygon",
        "coordinates": [[[8.4, 49.4], [8.5, 49.4], [8.5, 49.5], [8.4, 49.5], [8.4, 49.4]]]
      },
      "properties": {
        "datetime": "2024-06-15T10:30:31Z",
        "platform": "Sentinel-2A",
        "instruments": ["MSI"],
        "eo:cloud_cover": 15.2
      },
      "assets": {
        "thumbnail": {
          "href": "https://s3.amazonaws.com/basf-assets/thumbnails/S2A_20240615_T32UMA.jpg",
          "type": "image/jpeg",
          "roles": ["thumbnail"]
        },
        "data": {
          "href": "https://s3.amazonaws.com/basf-assets/data/S2A_20240615_T32UMA.tif",
          "type": "image/tiff; application=geotiff",
          "roles": ["data"]
        }
      }
    }
  ]
}
```

---

## AI Workflows API

### GET /ai-workflows
List available AI workflows.

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Environmental Anomaly Detection",
      "description": "Detect environmental anomalies in satellite imagery",
      "type": "ANOMALY_DETECTION",
      "status": "ACTIVE",
      "schedule": "0 0 6 * * *",
      "lastRun": "2024-06-15T06:00:00Z",
      "nextRun": "2024-06-16T06:00:00Z"
    }
  ]
}
```

### POST /ai-workflows
Create new AI workflow.

**Request Body:**
```json
{
  "name": "Air Quality Prediction",
  "description": "Predict air quality based on meteorological data",
  "type": "PREDICTION",
  "modelId": "air-quality-model-v1",
  "schedule": "0 0 */6 * * *",
  "parameters": {
    "inputSources": ["weather-stations", "satellite-data"],
    "outputFormat": "GeoJSON",
    "predictionHours": 24
  }
}
```

### POST /ai-workflows/{id}/execute
Execute AI workflow manually.

**Response:**
```json
{
  "executionId": "exec_123456789",
  "status": "RUNNING",
  "startTime": "2024-06-15T14:30:00Z",
  "estimatedDuration": 300
}
```

### GET /ai-workflows/{id}/executions
Get execution history for workflow.

**Response:**
```json
{
  "executions": [
    {
      "id": "exec_123456789",
      "status": "COMPLETED",
      "startTime": "2024-06-15T06:00:00Z",
      "endTime": "2024-06-15T06:05:30Z",
      "duration": 330,
      "results": {
        "itemsProcessed": 150,
        "anomaliesDetected": 3,
        "outputFiles": [
          "s3://basf-results/anomalies_20240615.geojson"
        ]
      }
    }
  ]
}
```

---

## User Management API

### GET /users
List system users (Admin only).

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@basf.com",
      "firstName": "System",
      "lastName": "Administrator",
      "roles": ["ADMIN"],
      "active": true,
      "lastLogin": "2024-06-15T08:30:00Z",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

### POST /users
Create new user account (Admin only).

**Request Body:**
```json
{
  "username": "analyst1",
  "email": "analyst1@basf.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "SecurePassword123!",
  "roles": ["ANALYST"]
}
```

### PUT /users/{id}
Update user information.

**Request Body:**
```json
{
  "email": "newemail@basf.com",
  "firstName": "John",
  "lastName": "Smith",
  "roles": ["ANALYST", "VIEWER"]
}
```

---

## AWS Services API

### GET /aws/s3/buckets
List available S3 buckets.

**Response:**
```json
{
  "buckets": [
    {
      "name": "javagis-basf-assets",
      "region": "eu-central-1",
      "creationDate": "2024-01-01T00:00:00Z",
      "size": 1024000000,
      "objectCount": 1500
    }
  ]
}
```

### POST /aws/s3/upload
Upload file to S3 bucket.

**Request:** Multipart form data
- `file`: File to upload
- `bucket`: Target bucket name
- `key`: Object key/path

### GET /aws/lambda/functions
List available Lambda functions.

**Response:**
```json
{
  "functions": [
    {
      "functionName": "process-satellite-image",
      "runtime": "python3.9",
      "description": "Process uploaded satellite imagery",
      "lastModified": "2024-06-15T10:00:00Z",
      "timeout": 300,
      "memorySize": 1024
    }
  ]
}
```

### POST /aws/lambda/invoke
Invoke Lambda function.

**Request Body:**
```json
{
  "functionName": "process-satellite-image",
  "payload": {
    "imageUrl": "s3://basf-assets/images/image.tif",
    "analysisType": "NDVI"
  }
}
```

---

## Error Handling

### Error Response Format
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request parameters",
    "details": [
      {
        "field": "acquisitionDate",
        "message": "Date must be in ISO 8601 format"
      }
    ],
    "timestamp": "2024-06-15T14:30:00Z",
    "path": "/api/satellite-images"
  }
}
```

### Common Error Codes
- `AUTHENTICATION_REQUIRED` (401): Missing or invalid authentication
- `INSUFFICIENT_PRIVILEGES` (403): User lacks required permissions
- `RESOURCE_NOT_FOUND` (404): Requested resource does not exist
- `VALIDATION_ERROR` (400): Invalid request parameters
- `PROCESSING_ERROR` (422): Unable to process request
- `RATE_LIMIT_EXCEEDED` (429): Too many requests
- `INTERNAL_SERVER_ERROR` (500): Unexpected server error

---

## Rate Limiting

API requests are subject to rate limiting to ensure fair usage and system stability:

- **Standard Users**: 1000 requests per hour
- **Premium Users**: 5000 requests per hour
- **Admin Users**: 10000 requests per hour

Rate limit headers are included in all responses:
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1624021200
```

---

## Examples

### Complete Workflow Example

```javascript
// 1. Authenticate
const authResponse = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'analyst1',
    password: 'password123'
  })
});
const { token } = await authResponse.json();

// 2. Search for satellite images
const searchResponse = await fetch('/api/satellite-images?startDate=2024-06-01&endDate=2024-06-30&platform=Sentinel-2', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const images = await searchResponse.json();

// 3. Trigger analysis workflow
const analysisResponse = await fetch(`/api/satellite-images/${images.content[0].id}/analyze`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    analysisType: 'NDVI',
    parameters: { outputFormat: 'GeoTIFF' }
  })
});

// 4. Check analysis status
const statusResponse = await fetch(`/api/ai-workflows/executions/${analysisResponse.executionId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
const status = await statusResponse.json();
```

### Python SDK Example

```python
import requests
from javagis_client import JavaGISClient

# Initialize client
client = JavaGISClient(base_url='https://api.javagis-basf.com/api')

# Authenticate
client.login('analyst1', 'password123')

# Search satellite images
images = client.satellite_images.search(
    start_date='2024-06-01',
    end_date='2024-06-30',
    platform='Sentinel-2',
    cloud_cover_max=20
)

# Upload shapefile
shapefile = client.shapefiles.upload(
    shp_file='facility_boundaries.shp',
    metadata={'category': 'FACILITY_BOUNDARIES'}
)

# Create AI workflow
workflow = client.ai_workflows.create(
    name='Daily Environmental Monitoring',
    type='ANOMALY_DETECTION',
    schedule='0 0 6 * * *'
)
```

---

*For additional API documentation, examples, and SDK downloads, visit the developer portal at https://developers.javagis.com*

