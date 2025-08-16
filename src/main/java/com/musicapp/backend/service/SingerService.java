// File: src/main/java/com/musicapp/backend/service/SingerService.java
package com.musicapp.backend.service;

import com.musicapp.backend.dto.singer.*;
import com.musicapp.backend.dto.song.SongDto;
import com.musicapp.backend.entity.Singer;
import com.musicapp.backend.entity.Singer.SingerStatus;
import com.musicapp.backend.entity.Song;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceAlreadyExistsException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.mapper.SingerMapper;
import com.musicapp.backend.mapper.SongMapper;
import com.musicapp.backend.repository.SingerRepository;
import com.musicapp.backend.repository.SongRepository;
import com.musicapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SingerService {

    private final SingerRepository singerRepository;
    private final UserRepository userRepository;
    private final SingerMapper singerMapper;
    private final FileStorageService fileStorageService;
    private final SongRepository songRepository;
    private final SongMapper songMapper;

    @Transactional(readOnly = true)
    public Page<SingerDto> getAllSingersForAdmin(String keyword, Pageable pageable, Singer.SingerStatus status) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return singerRepository.searchAllWithSongCountForAdmin(keyword.trim(), pageable, status);
        } else {
            return singerRepository.findAllWithSongCountForAdmin(pageable, status);
        }
    }

    @Transactional
    public SingerDto createSingerByAdmin(AdminCreateSingerRequest request, MultipartFile avatarFile) {
        if (singerRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Singer already exists with email: " + request.getEmail());
        }

        String avatarPath = null;
        if (avatarFile != null && !avatarFile.isEmpty()) {
            avatarPath = fileStorageService.storeFile(avatarFile, "images/singers");
        }

        Singer singer = Singer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .avatarPath(avatarPath)
                .creator(null)
                .status(Singer.SingerStatus.APPROVED)
                .build();

        Singer savedSinger = singerRepository.save(singer);
        return singerMapper.toDto(savedSinger);
    }

    @Transactional
    public List<SingerDto> createMultipleSingersByAdmin(AdminCreateMultipleSingersRequest request, List<MultipartFile> avatarFiles, User admin) {
        List<Singer> newSingers = new ArrayList<>();
        Map<String, MultipartFile> fileMap = (avatarFiles != null)
                ? avatarFiles.stream().collect(Collectors.toMap(MultipartFile::getOriginalFilename, Function.identity()))
                : Collections.emptyMap();

        for (AdminCreateMultipleSingersRequest.SingerInfo singerInfo : request.getSingers()) {
            if (singerRepository.existsByEmail(singerInfo.getEmail())) {
                throw new ResourceAlreadyExistsException("Singer already exists with email: " + singerInfo.getEmail());
            }

            String avatarPath = null;
            // Dùng clientId để map với tên file gốc
            MultipartFile avatarFile = fileMap.get(singerInfo.getClientId());
            if (avatarFile != null && !avatarFile.isEmpty()) {
                avatarPath = fileStorageService.storeFile(avatarFile, "images/singers");
            }

            Singer singer = Singer.builder()
                    .name(singerInfo.getName())
                    .email(singerInfo.getEmail())
                    .avatarPath(avatarPath)
                    .creator(admin)
                    .status(Singer.SingerStatus.APPROVED)
                    .build();
            newSingers.add(singer);
        }

        List<Singer> savedSingers = singerRepository.saveAll(newSingers);
        return savedSingers.stream()
                .map(singerMapper::toDto)
                .collect(Collectors.toList());
    }
    // END-CHANGE

    @Transactional
    public SingerDto updateSingerByAdmin(Long id, AdminUpdateSingerRequest request, MultipartFile avatarFile) {
        Singer singer = singerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca sĩ với ID: " + id));

        if (StringUtils.hasText(request.getName()) && !singer.getName().equals(request.getName())) {
            singer.setName(request.getName());
        }

        if (StringUtils.hasText(request.getEmail()) && !singer.getEmail().equals(request.getEmail())) {
            singerRepository.findByEmail(request.getEmail()).ifPresent(existingSinger -> {
                if (!existingSinger.getId().equals(id)) {
                    throw new ResourceAlreadyExistsException("Email '" + request.getEmail() + "' đã được sử dụng bởi một ca sĩ khác.");
                }
            });
            singer.setEmail(request.getEmail());
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            if (singer.getAvatarPath() != null) {
                fileStorageService.deleteFile(singer.getAvatarPath());
            }
            String newAvatarPath = fileStorageService.storeFile(avatarFile, "images/singers");
            singer.setAvatarPath(newAvatarPath);
        }

        Singer updatedSinger = singerRepository.save(singer);
        return singerMapper.toDto(updatedSinger);
    }

    @Transactional
    public void deleteSingerByAdmin(Long id) {
        Singer singer = singerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca sĩ với ID: " + id));

        if (singer.getStatus() != Singer.SingerStatus.APPROVED) {
            throw new BadRequestException("Chỉ có thể xóa các ca sĩ đang ở trạng thái APPROVED.");
        }

        long songCount = songRepository.countBySingersContains(singer);
        if (songCount > 0) {
            throw new BadRequestException("Không thể xóa ca sĩ '" + singer.getName() + "' vì ca sĩ này đang được liên kết với " + songCount + " bài hát.");
        }

        if (singer.getAvatarPath() != null) {
            fileStorageService.deleteFile(singer.getAvatarPath());
        }
        singerRepository.delete(singer);
    }

    private boolean hasAdminRole(User user) {
        return user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    @Transactional(readOnly = true)
    public List<SingerDto> getSelectableSingersForCreator(User user) {
        List<Singer> singers;
        if (hasAdminRole(user)) {
            singers = singerRepository.findByStatusOrderByNameAsc(SingerStatus.APPROVED);
        } else {
            singers = singerRepository.findByCreatorIdAndStatusOrderByNameAsc(user.getId(), SingerStatus.APPROVED);
        }
        return singers.stream()
                .map(singerMapper::toDtoWithoutSongCount)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SingerDto> getAllSingers(Pageable pageable) {
        return singerRepository.findAllWithSongCount(pageable);
    }

    @Transactional(readOnly = true)
    public Page<SingerDto> searchSingers(String keyword, Pageable pageable) {
        Page<Singer> singerPage = singerRepository.searchApprovedSingersByName(keyword, pageable);
        return singerPage.map(singerMapper::toDto);
    }

    @Transactional(readOnly = true)
    public SingerDetailDto getSingerDetailById(Long id) {
        Singer singer = singerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca sĩ với ID: " + id));

        List<Song> songs = songRepository.findBySingersIdAndStatus(id, Song.SongStatus.APPROVED);
        songs.sort(Comparator.comparing(Song::getCreatedAt).reversed());

        List<SongDto> songDtos = songs.stream()
                .map(song -> songMapper.toDto(song, null))
                .collect(Collectors.toList());

        return singerMapper.toDetailDto(singer, songDtos);
    }

    @Transactional(readOnly = true)
    public List<SingerDto> getAllSingersAsList() {
        return singerRepository.findAllOrderByNameAsc(Pageable.unpaged())
                .getContent()
                .stream()
                .map(singerMapper::toDtoWithoutSongCount)
                .collect(Collectors.toList());
    }
}