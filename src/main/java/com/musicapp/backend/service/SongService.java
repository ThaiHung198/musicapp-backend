package com.musicapp.backend.service;

import com.musicapp.backend.dto.song.CreateSongRequest;
import com.musicapp.backend.dto.song.SongDto;
import com.musicapp.backend.dto.song.UpdateSongRequest;
import com.musicapp.backend.entity.*;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.exception.UnauthorizedException;
import com.musicapp.backend.mapper.SongMapper;
import com.musicapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SongService {

    private final SongRepository songRepository;
    private final SingerRepository singerRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService; // Dependency đã được thêm vào
    private final SongMapper songMapper;

    public Page<SongDto> getAllApprovedSongs(Pageable pageable, User currentUser) {
        return songRepository.findByStatusOrderByCreatedAtDesc(Song.SongStatus.APPROVED, pageable)
                .map(song -> songMapper.toDto(song, currentUser));
    }

    public Page<SongDto> searchSongs(String keyword, Pageable pageable, User currentUser) {
        return songRepository.searchApprovedSongs(keyword, Song.SongStatus.APPROVED, pageable)
                .map(song -> songMapper.toDto(song, currentUser));
    }

    public SongDto getSongById(Long id, User currentUser) {
        Song song = songRepository.findByIdAndStatus(id, Song.SongStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + id));
        return songMapper.toDto(song, currentUser);
    }

    public SongDto getSongByIdForCreator(Long id, User creator) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + id));

        // Check if user is the creator or admin
        if (!song.getCreator().getId().equals(creator.getId()) &&
                !hasAdminRole(creator)) {
            throw new UnauthorizedException("You don't have permission to access this song");
        }

        return songMapper.toDto(song, creator);
    }

    public Page<SongDto> getUserCreatedSongs(Long userId, Pageable pageable, User currentUser) {
        return songRepository.findByCreatorIdOrderByCreatedAtDesc(userId, pageable)
                .map(song -> songMapper.toDto(song, currentUser));
    }

    public Page<SongDto> getSongsBySinger(Long singerId, Pageable pageable, User currentUser) {
        return songRepository.findBySingerIdAndApproved(singerId, pageable)
                .map(song -> songMapper.toDto(song, currentUser));
    }

    public List<SongDto> getTopSongsByListenCount(int limit, User currentUser) {
        Pageable pageable = PageRequest.of(0, limit);
        return songRepository.findTopSongsByListenCount(pageable)
                .stream()
                .map(song -> songMapper.toDto(song, currentUser))
                .collect(Collectors.toList());
    }

    public List<SongDto> getRecentlyCreatedSongs(int limit, User currentUser) {
        Pageable pageable = PageRequest.of(0, limit);
        return songRepository.findRecentlyCreatedSongs(pageable)
                .stream()
                .map(song -> songMapper.toDto(song, currentUser))
                .collect(Collectors.toList());
    }

    public List<SongDto> getMostLikedSongs(int limit, User currentUser) {
        Pageable pageable = PageRequest.of(0, limit);
        return songRepository.findMostLikedSongs(pageable)
                .stream()
                .map(song -> songMapper.toDto(song, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional
    public SongDto createSong(CreateSongRequest request, User creator) {
        // Validate singers exist
        Set<Singer> singers = new HashSet<>();
        for (Long singerId : request.getSingerIds()) {
            Singer singer = singerRepository.findById(singerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Singer not found with id: " + singerId));
            singers.add(singer);
        }

        // Validate tags exist (if provided)
        Set<Tag> tags = new HashSet<>();
        if (request.getTagIds() != null) {
            for (Long tagId : request.getTagIds()) {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));
                tags.add(tag);
            }
        }

        Song song = Song.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .filePath(request.getFilePath())
                .thumbnailPath(request.getThumbnailPath())
                .creator(creator)
                .singers(singers)
                .tags(tags)
                .isPremium(request.getIsPremium()) // Sử dụng isPremium từ request
                .status(Song.SongStatus.PENDING)
                .build();

        Song savedSong = songRepository.save(song);
        return songMapper.toDto(savedSong, creator);
    }

    @Transactional
    public SongDto updateSong(Long id, UpdateSongRequest request, User user) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + id));

        // Check permission
        if (!song.getCreator().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new UnauthorizedException("You don't have permission to update this song");
        }

        // Update fields if provided
        if (request.getTitle() != null) {
            song.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            song.setDescription(request.getDescription());
        }
        if (request.getThumbnailPath() != null) {
            song.setThumbnailPath(request.getThumbnailPath());
        }

        // Update singers if provided
        if (request.getSingerIds() != null) {
            Set<Singer> singers = new HashSet<>();
            for (Long singerId : request.getSingerIds()) {
                Singer singer = singerRepository.findById(singerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Singer not found with id: " + singerId));
                singers.add(singer);
            }
            song.setSingers(singers);
        }

        // Update tags if provided
        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>();
            for (Long tagId : request.getTagIds()) {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));
                tags.add(tag);
            }
            song.setTags(tags);
        }

        // Reset status to pending if creator updated (admin doesn't need re-approval)
        if (!hasAdminRole(user)) {
            song.setStatus(Song.SongStatus.PENDING);
        }

        Song updatedSong = songRepository.save(song);
        return songMapper.toDto(updatedSong, user);
    }

    @Transactional
    public void deleteSong(Long id, User user) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + id));

        // Check permission
        if (!song.getCreator().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new UnauthorizedException("You don't have permission to delete this song");
        }

        songRepository.delete(song);
    }

    @Transactional
    public SongDto approveSong(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + id));

        song.setStatus(Song.SongStatus.APPROVED);
        Song approvedSong = songRepository.save(song);
        return songMapper.toDto(approvedSong, null);
    }

    @Transactional
    public SongDto rejectSong(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + id));

        song.setStatus(Song.SongStatus.REJECTED);
        Song rejectedSong = songRepository.save(song);
        return songMapper.toDto(rejectedSong, null);
    }

    @Transactional
    public void incrementListenCount(Long id) {
        songRepository.incrementListenCount(id);
    }

    public Page<SongDto> getPendingSongs(Pageable pageable) {
        return songRepository.findByStatusOrderByCreatedAtDesc(Song.SongStatus.PENDING, pageable)
                .map(song -> songMapper.toDto(song, null));
    }

    public boolean canUserAccessSong(Long songId, String username) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + songId));

        // Free songs are accessible to everyone
        if (!song.getIsPremium()) {
            return true;
        }

        // For premium songs, user must be logged in
        if (username == null) {
            return false;
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        // Access is granted only via an active subscription.
        return subscriptionService.hasActivePremiumSubscription(user.getId());
    }

    public SongDto getSongWithAccessCheck(Long id, String username) {
        Song song = songRepository.findByIdAndStatus(id, Song.SongStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + id));

        User user = null;
        if (username != null) {
            user = userRepository.findByEmail(username).orElse(null);
        }

        return songMapper.toDto(song, user);
    }

    private boolean hasAdminRole(User user) {
        return user.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }
}