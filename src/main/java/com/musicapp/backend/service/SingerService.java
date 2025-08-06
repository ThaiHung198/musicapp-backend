package com.musicapp.backend.service;

import com.musicapp.backend.dto.singer.CreateSingerRequest;
import com.musicapp.backend.dto.singer.SingerDto;
import com.musicapp.backend.entity.Singer;
import com.musicapp.backend.entity.User;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SingerService {

    private final SingerRepository singerRepository;
    private final UserRepository userRepository;
    private final SingerMapper singerMapper;

    public Page<SingerDto> getAllSingers(Pageable pageable) {
        return singerRepository.findAllOrderByNameAsc(pageable)
                .map(singerMapper::toDto);
    }

    public Page<SingerDto> searchSingers(String keyword, Pageable pageable) {
        return singerRepository.findByNameContainingIgnoreCaseOrderByNameAsc(keyword, pageable)
                .map(singerMapper::toDto);
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

        // Check if singer already exists by name or email
        if (singerRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ResourceAlreadyExistsException("Singer already exists with name: " + request.getName());
        }
        if (singerRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Singer already exists with email: " + request.getEmail());
        }

        Singer singer = Singer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .avatarPath(request.getAvatarPath())
                .creator(creator)
                .build();

        Singer savedSinger = singerRepository.save(singer);
        return singerMapper.toDto(savedSinger);
    }

    @Transactional
    public SingerDto updateSinger(Long id, CreateSingerRequest request) {
        Singer singer = singerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Singer not found with id: " + id));

        // Check if new name conflicts with another existing singer
        singerRepository.findByName(request.getName()).ifPresent(existingSinger -> {
            if (!existingSinger.getId().equals(id)) {
                throw new ResourceAlreadyExistsException("Another singer already exists with name: " + request.getName());
            }
        });

        // Check if new email conflicts with another existing singer
        singerRepository.findByEmail(request.getEmail()).ifPresent(existingSinger -> {
            if (!existingSinger.getId().equals(id)) {
                throw new ResourceAlreadyExistsException("Another singer already exists with email: " + request.getEmail());
            }
        });

        singer.setName(request.getName());
        singer.setEmail(request.getEmail());
        singer.setAvatarPath(request.getAvatarPath());

        Singer updatedSinger = singerRepository.save(singer);
        return singerMapper.toDto(updatedSinger);
    }

    @Transactional
    public void deleteSinger(Long id) {
        if (!singerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Singer not found with id: " + id);
        }
        singerRepository.deleteById(id);
    }
}