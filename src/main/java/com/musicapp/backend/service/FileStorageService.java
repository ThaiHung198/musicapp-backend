// backend/src/main/java/com/musicapp/backend/service/FileStorageService.java

package com.musicapp.backend.service;

import com.musicapp.backend.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public String storeFile(MultipartFile file, String subfolder) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        if (originalFileName.contains("..")) {
            throw new BadRequestException("Sorry! Filename contains invalid path sequence " + originalFileName);
        }

        try {
            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf(".");
            if (lastDotIndex >= 0) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            String key = subfolder + "/" + newFileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType()) // Thêm content type khi upload
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return "/uploads/" + key;

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            String key = fileUrl.substring(fileUrl.indexOf("/uploads/") + 9);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            System.err.println("Could not delete file from S3: " + fileUrl + ". Reason: " + e.getMessage());
        }
    }

    public GetObjectResponse getS3ObjectAttributes(String key) {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);

        return GetObjectResponse.builder()
                .contentLength(headObjectResponse.contentLength())
                .contentType(headObjectResponse.contentType())
                .build();
    }

    public void getS3ObjectRange(String key, String range, OutputStream outputStream) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .range(range)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3is = s3Client.getObject(getObjectRequest)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = s3is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error streaming S3 object", e);
        }
    }
}