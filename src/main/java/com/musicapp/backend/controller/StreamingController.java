// backend/src/main/java/com/musicapp/backend/controller/StreamingController.java

package com.musicapp.backend.controller;

import com.musicapp.backend.entity.Song;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.repository.SongRepository;
import com.musicapp.backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
@Slf4j
public class StreamingController {

    private final FileStorageService fileStorageService;
    private final SongRepository songRepository;

    @GetMapping("/songs/{id}")
    public ResponseEntity<StreamingResponseBody> streamSong(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String httpRangeHeader) {

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + id));

        String key = song.getFilePath().substring("/uploads/".length());

        try {
            GetObjectResponse s3ObjectAttributes = fileStorageService.getS3ObjectAttributes(key);
            long fileSize = s3ObjectAttributes.contentLength();
            String contentType = s3ObjectAttributes.contentType();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", contentType);
            headers.add("Accept-Ranges", "bytes");

            if (httpRangeHeader == null) {
                // Gửi toàn bộ file nếu không có Range header
                headers.add("Content-Length", String.valueOf(fileSize));
                StreamingResponseBody responseBody = outputStream -> {
                    fileStorageService.getS3ObjectRange(key, "bytes=0-" + (fileSize - 1), outputStream);
                };
                return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
            }

            // Xử lý Range header
            String[] ranges = httpRangeHeader.substring("bytes=".length()).split("-");
            long rangeStart = Long.parseLong(ranges[0]);
            long rangeEnd = ranges.length > 1 ? Long.parseLong(ranges[1]) : fileSize - 1;

            if (rangeStart > fileSize || rangeEnd >= fileSize) {
                rangeEnd = fileSize - 1;
            }

            long contentLength = (rangeEnd - rangeStart) + 1;
            headers.add("Content-Length", String.valueOf(contentLength));
            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize);

            StreamingResponseBody responseBody = outputStream -> {
                fileStorageService.getS3ObjectRange(key, httpRangeHeader, outputStream);
            };

            return new ResponseEntity<>(responseBody, headers, HttpStatus.PARTIAL_CONTENT);

        } catch (Exception e) {
            log.error("Error streaming song with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}