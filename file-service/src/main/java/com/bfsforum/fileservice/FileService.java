package com.bfsforum.fileservice;

import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class FileService {
    @Value("${s3.bucket.name}")
    private String s3BucketName;

    private final S3Template s3Template; // For general S3 operations (though not directly used for presigning here)
    private final S3Presigner s3Presigner; // The dedicated client for generating pre-signed URLs

    public FileService(S3Template s3Template, S3Presigner s3Presigner) {
        this.s3Template = s3Template;
        this.s3Presigner = s3Presigner;
    }

    /**
     * Generates a pre-signed URL for uploading an object to S3.
     * This URL allows a client to perform an HTTP PUT request directly to S3.
     * The object will be stored under a dynamically generated key (e.g., "uploads/UUID_originalFileName").
     *
     * @param originalFilename The original name of the file (e.g., "document.pdf").
     * @param contentType The Content-Type of the file (e.g., "application/pdf", "image/png").
     * This must match the Content-Type header the client sends during the PUT request.
     * @param expirationMinutes The duration in minutes for which the URL will be valid.
     * @return The pre-signed URL as a String.
     */
    public String generatePreSignedUploadUrl(String originalFilename, String contentType, int expirationMinutes) {
        // Generate a unique object key to avoid overwrites and organize uploads
        String objectKey = "uploads/" + UUID.randomUUID().toString() + "_" + originalFilename;
        Duration expiry = Duration.ofMinutes(expirationMinutes);

        // Build the PutObjectRequest specifying the bucket, key, and content type.
        // It's crucial to set Content-Type here if you expect the client to use it.
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(s3BucketName)
            .key(objectKey)
            .contentType(contentType)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build();

        // Generate the pre-signed URL
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(r -> r
            .putObjectRequest(putObjectRequest)
            .signatureDuration(expiry)
        );

        return presignedRequest.url().toString();
    }

    /**
     * Generates a pre-signed URL for downloading an object from S3.
     * This URL allows a client to perform an HTTP GET request directly from S3.
     *
     * @param objectKey The full key (path) of the object in the S3 bucket (e.g., "uploads/uuid_document.pdf").
     * @param expirationMinutes The duration in minutes for which the URL will be valid.
     * @return The pre-signed URL as a String.
     */
    public String generatePreSignedDownloadUrl(String objectKey, int expirationMinutes) {
        Duration expiry = Duration.ofMinutes(expirationMinutes);

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(r -> r
            .getObjectRequest(b -> b.bucket(s3BucketName).key(objectKey))
            .signatureDuration(expiry)
        );

        return presignedRequest.url().toString();
    }
}
