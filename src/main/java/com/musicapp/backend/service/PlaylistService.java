package com.musicapp.backend.service;

import com.musicapp.backend.dto.playlist.CreatePlaylistRequest;
import com.musicapp.backend.dto.playlist.PlaylistDto;
import com.musicapp.backend.entity.Playlist;
import com.musicapp.backend.entity.Playlist.PlaylistVisibility;
import com.musicapp.backend.entity.User;

import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.exception.UnauthorizedException;

import com.musicapp.backend.mapper.PlaylistMapper;
import com.musicapp.backend.repository.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final FileStorageService fileStorageService;
    private final PlaylistMapper playlistMapper;

    @Transactional
    public PlaylistDto createPlaylist(CreatePlaylistRequest request, MultipartFile thumbnailFile, User currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        String thumbnailPath = null;
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            thumbnailPath = fileStorageService.storeFile(thumbnailFile, "images/playlists");
        }

        Playlist playlist = Playlist.builder()
                .name(request.getName())
                .thumbnailPath(thumbnailPath)
                .songs(new HashSet<>())
                .likes(new HashSet<>())
                .comments(new HashSet<>())
                .creator(isAdmin ? null : currentUser)
                .visibility(isAdmin ? PlaylistVisibility.PUBLIC : PlaylistVisibility.PRIVATE)
                .build();

        Playlist savedPlaylist = playlistRepository.save(playlist);

        return playlistMapper.toDto(savedPlaylist, currentUser);
    }
    @Transactional(readOnly = true)
    public PlaylistDto getPlaylistById(Long playlistId, User currentUser) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        if (playlist.getVisibility() == Playlist.PlaylistVisibility.PRIVATE) {
            if (currentUser == null) {
                throw new UnauthorizedException("Bạn phải đăng nhập để xem playlist này.");
            }

            boolean isOwner = playlist.getCreator() != null &&
                    playlist.getCreator().getId().equals(currentUser.getId());

            if (!isOwner) {
                throw new UnauthorizedException("Bạn không có quyền xem playlist này.");
            }
        }

        return playlistMapper.toDto(playlist, currentUser);
    }


    public List<PlaylistDto> getMyPlaylists(User currentUser) {
        List<PlaylistDto> playlists = playlistRepository.findPlaylistsByCreator(currentUser);
        playlists.forEach(p -> p.setCreatorName(currentUser.getUsername()));
        return playlists;
    }

}