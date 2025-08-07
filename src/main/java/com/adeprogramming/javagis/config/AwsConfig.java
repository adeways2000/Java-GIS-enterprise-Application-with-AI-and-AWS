package com.adeprogramming.javagis.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for AWS services.
 * Provides beans for S3, Lambda, and CloudWatch clients.
 */
@Configuration
public class AwsConfig {

    @Value("${javagis.aws.access-key}")
    private String accessKey;

    @Value("${javagis.aws.secret-key}")
    private String secretKey;

    @Value("${javagis.aws.region}")
    private String region;

    /**
     * Create AWS credentials.
     *
     * @return AWS credentials
     */
    @Bean
    public AWSCredentials awsCredentials() {
        return new BasicAWSCredentials(accessKey, secretKey);
    }

    /**
     * Create Amazon S3 client.
     *
     * @param awsCredentials AWS credentials
     * @return Amazon S3 client
     */
    @Bean
    public AmazonS3 amazonS3(AWSCredentials awsCredentials) {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.fromName(region))
                .build();
    }

    /**
     * Create AWS Lambda client.
     *
     * @param awsCredentials AWS credentials
     * @return AWS Lambda client
     */
    @Bean
    public AWSLambda awsLambda(AWSCredentials awsCredentials) {
        return AWSLambdaClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.fromName(region))
                .build();
    }

    /**
     * Create Amazon CloudWatch client.
     *
     * @param awsCredentials AWS credentials
     * @return Amazon CloudWatch client
     */
    @Bean
    public AmazonCloudWatch amazonCloudWatch(AWSCredentials awsCredentials) {
        return AmazonCloudWatchClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.fromName(region))
                .build();
    }
}


