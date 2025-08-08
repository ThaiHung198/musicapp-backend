package com.musicapp.backend.service;

import com.musicapp.backend.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, String subfolder) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            if (originalFileName.contains("..")) {
                throw new BadRequestException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            Path targetSubfolder = this.fileStorageLocation.resolve(subfolder);
            Files.createDirectories(targetSubfolder);

            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf(".");
            if (lastDotIndex >= 0) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            Path finalPath = targetSubfolder.resolve(newFileName);
            Files.copy(file.getInputStream(), finalPath, StandardCopyOption.REPLACE_EXISTING);

            // <<< SỬA LỖI: Luôn trả về đường dẫn bắt đầu bằng /uploads/ >>>
            return "/uploads/" + subfolder + "/" + newFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }
}