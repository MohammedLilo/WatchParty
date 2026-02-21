package com.lilo.service;

import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;

@Profile("uat")
@Slf4j
@RequiredArgsConstructor
@Service
//@Primary
public class S3FileStorageService implements FileStorageService {

    private final S3Template s3Template;
    private final S3Presigner s3Presigner;

    @Value("${app.aws.bucket-name}")
    private String bucketName;
    @Value("${app.aws.presigned-url-expiration-in-minutes}")
    private int presignedUrlExpiration;

    @Override
    public void save(String fileName, MultipartFile file) throws IOException {
        try {
            s3Template.upload(bucketName, fileName, file.getInputStream());
        } catch (S3Exception e) {
            log.error("AWS S3 error message: {}", e.getMessage());
            log.error("AWS cause: ", e.getCause());
            throw e;
        }
    }

    @Override
    public Resource load(String fileName) {
        return s3Template.download(bucketName, fileName);
    }

    @Override
    public void delete(String fileName) {
        s3Template.deleteObject(bucketName, fileName);
    }
    @Override
    public String getDownloadUrl(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlExpiration))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}
