package com.musicapp.backend.service;

import com.musicapp.backend.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // THAY ĐỔI QUAN TRỌNG: Trả về đường dẫn tương đối để phục vụ qua backend
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
            // Giả sử fileUrl có dạng /uploads/images/songs/filename.jpg
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
}