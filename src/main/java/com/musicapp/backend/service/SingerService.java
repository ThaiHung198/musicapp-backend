package com.musicapp.backend.service;

import com.musicapp.backend.dto.singer.AdminCreateSingerRequest;
import com.musicapp.backend.dto.singer.CreateSingerRequest;
import com.musicapp.backend.dto.singer.SingerDto;
import com.musicapp.backend.entity.Singer;
import com.musicapp.backend.entity.Singer.SingerStatus;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceAlreadyExistsException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.mapper.SingerMapper;
import com.musicapp.backend.repository.SingerRepository;
import com.musicapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.musicapp.backend.dto.singer.AdminUpdateSingerRequest;
import org.springframework.util.StringUtils;

import com.musicapp.backend.repository.SubmissionSingersRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SingerService {

    private final SingerRepository singerRepository;
    private final UserRepository userRepository;
    private final SingerMapper singerMapper;
    private final FileStorageService fileStorageService;
    private final SubmissionSingersRepository submissionSingersRepository;

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

    public List<SingerDto> getSelectableSingersForCreator(User creator) {
        return singerRepository.findByCreatorIdAndStatusOrStatus(
                        creator.getId(),
                        SingerStatus.APPROVED
                )
                .stream()
                .map(singerMapper::toDtoWithoutSongCount)
                .collect(Collectors.toList());
    }

    public Page<SingerDto> getAllSingers(Pageable pageable) {
        return singerRepository.findAllWithSongCount(pageable);
    }

    public Page<SingerDto> searchSingers(String keyword, Pageable pageable) {
        return singerRepository.searchAllWithSongCount(keyword, pageable);
    }

    public SingerDto getSingerById(Long id) {
        Singer singer = singerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Singer not found with id: " + id));
        return singerMapper.toDto(singer);
    }

    public List<SingerDto> getAllSingersAsList() {
        return singerRepository.findAllOrderByNameAsc(Pageable.unpaged())
                .getContent()
                .stream()
                .map(singerMapper::toDtoWithoutSongCount)
                .collect(Collectors.toList());
    }

    @Transactional
    public SingerDto createSinger(CreateSingerRequest request, String creatorUsername) {
        User creator = userRepository.findByEmail(creatorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found with email: " + creatorUsername));

        if (singerRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Singer already exists with email: " + request.getEmail());
        }

        Singer singer = Singer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .avatarPath(request.getAvatarPath())
                .creator(creator)
                .status(SingerStatus.PENDING)
                .build();

        Singer savedSinger = singerRepository.save(singer);
        return singerMapper.toDto(savedSinger);
    }

    @Transactional
    public SingerDto updateSinger(Long id, AdminUpdateSingerRequest request, MultipartFile avatarFile) {
        // 1. Lấy ca sĩ từ DB
        Singer singer = singerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca sĩ với ID: " + id));

        // 2. Cập nhật tên nếu được cung cấp
        if (StringUtils.hasText(request.getName()) && !singer.getName().equals(request.getName())) {
            singer.setName(request.getName());
        }

        // 3. Cập nhật email nếu được cung cấp và kiểm tra trùng lặp
        if (StringUtils.hasText(request.getEmail()) && !singer.getEmail().equals(request.getEmail())) {
            singerRepository.findByEmail(request.getEmail()).ifPresent(existingSinger -> {
                throw new ResourceAlreadyExistsException("Email '" + request.getEmail() + "' đã được sử dụng bởi một ca sĩ khác.");
            });
            singer.setEmail(request.getEmail());
        }

        // 4. Xử lý cập nhật ảnh đại diện
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (singer.getAvatarPath() != null) {
                fileStorageService.deleteFile(singer.getAvatarPath());
            }
            // Lưu ảnh mới và cập nhật đường dẫn
            String newAvatarPath = fileStorageService.storeFile(avatarFile, "images/singers");
            singer.setAvatarPath(newAvatarPath);
        }

        // 5. Lưu lại vào DB và trả về kết quả
        Singer updatedSinger = singerRepository.save(singer);
        return singerMapper.toDto(updatedSinger);
    }

    @Transactional
    public void deleteSinger(Long id) {
        // 1. Kiểm tra ca sĩ tồn tại
        Singer singer = singerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca sĩ với ID: " + id));

        // 2. KIỂM TRA TRẠNG THÁI CỦA CA SĨ
        if (singer.getStatus() != Singer.SingerStatus.APPROVED) {
            throw new BadRequestException("Chỉ có thể xóa các ca sĩ đang ở trạng thái APPROVED.");
        }

        // 3. Kiểm tra ca sĩ có bài hát nào không
        long songCount = singerRepository.countSongsBySingerId(id);
        if (songCount > 0) {
            throw new BadRequestException("Không thể xóa ca sĩ '" + singer.getName() + "' vì ca sĩ này đang có " + songCount + " bài hát trong hệ thống.");
        }

        // 4. Kiểm tra ca sĩ có trong yêu cầu (submission) nào không
        long submissionCount = submissionSingersRepository.countBySingerId(id);
        if (submissionCount > 0) {
            throw new BadRequestException("Không thể xóa ca sĩ '" + singer.getName() + "' vì ca sĩ này đang có trong một yêu cầu chờ duyệt hoặc đã bị từ chối.");
        }

        // 5. Nếu ổn, tiến hành xóa file ảnh và xóa ca sĩ
        if (singer.getAvatarPath() != null) {
            fileStorageService.deleteFile(singer.getAvatarPath());
        }
        singerRepository.delete(singer);
    }
}