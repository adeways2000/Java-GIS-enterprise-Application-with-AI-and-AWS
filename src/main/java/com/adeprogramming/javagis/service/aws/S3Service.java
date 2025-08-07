package com.adeprogramming.javagis.service.aws;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Service for interacting with Amazon S3.
 * Provides methods for uploading, downloading, and managing files in S3.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${javagis.storage.s3.bucket}")
    private String bucketName;

    /**
     * Upload a file to S3.
     *
     * @param file The file to upload
     * @param key The S3 key (path) for the file
     * @return The S3 URI of the uploaded file
     * @throws IOException if there is an error reading the file
     */
    public String uploadFile(MultipartFile file, String key) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));
        }

        return "s3://" + bucketName + "/" + key;
    }

    /**
     * Upload a byte array to S3.
     *
     * @param data The byte array to upload
     * @param key The S3 key (path) for the file
     * @param contentType The content type of the data
     * @return The S3 URI of the uploaded file
     */
    public String uploadFile(byte[] data, String key, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(data.length);
        metadata.setContentType(contentType);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            amazonS3.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));
        } catch (IOException e) {
            log.error("Error closing input stream", e);
        }

        return "s3://" + bucketName + "/" + key;
    }

    /**
     * Download a file from S3.
     *
     * @param key The S3 key (path) of the file
     * @return The file content as a byte array
     * @throws IOException if there is an error reading the file
     */
    public byte[] downloadFile(String key) throws IOException {
        S3Object s3Object = amazonS3.getObject(bucketName, key);
        try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    /**
     * Generate a pre-signed URL for a file in S3.
     *
     * @param key The S3 key (path) of the file
     * @param expirationMinutes The expiration time in minutes
     * @return The pre-signed URL
     */
    public URL generatePresignedUrl(String key, int expirationMinutes) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * expirationMinutes;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
    }

    /**
     * Delete a file from S3.
     *
     * @param key The S3 key (path) of the file
     */
    public void deleteFile(String key) {
        amazonS3.deleteObject(bucketName, key);
    }

    /**
     * List files in an S3 directory.
     *
     * @param prefix The directory prefix
     * @return List of S3 keys in the directory
     */
    public List<String> listFiles(String prefix) {
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(prefix);

        ListObjectsV2Result result;
        List<String> keys = new ArrayList<>();

        do {
            result = amazonS3.listObjectsV2(request);

            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                keys.add(objectSummary.getKey());
            }

            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());

        return keys;
    }

    /**
     * Check if a file exists in S3.
     *
     * @param key The S3 key (path) of the file
     * @return True if the file exists, false otherwise
     */
    public boolean fileExists(String key) {
        return amazonS3.doesObjectExist(bucketName, key);
    }

    /**
     * Generate a unique S3 key for a file.
     *
     * @param prefix The directory prefix
     * @param fileName The original file name
     * @return A unique S3 key
     */
    public String generateUniqueKey(String prefix, String fileName) {
        return prefix + "/" + UUID.randomUUID() + "/" + fileName;
    }

    /**
     * Copy a file within S3.
     *
     * @param sourceKey The source S3 key
     * @param destinationKey The destination S3 key
     * @return The S3 URI of the copied file
     */
    public String copyFile(String sourceKey, String destinationKey) {
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucketName, sourceKey, bucketName, destinationKey);
        amazonS3.copyObject(copyObjectRequest);
        return "s3://" + bucketName + "/" + destinationKey;
    }
}

