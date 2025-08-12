package com.musicapp.backend.service;

import com.musicapp.backend.dto.playlist.CreatePlaylistRequest;
import com.musicapp.backend.dto.playlist.PlaylistDto;
import com.musicapp.backend.entity.Playlist;
import com.musicapp.backend.entity.Playlist.PlaylistVisibility;
import com.musicapp.backend.entity.Song;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.mapper.PlaylistMapper;
import com.musicapp.backend.repository.PlaylistRepository;
import com.musicapp.backend.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
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

        Set<Song> songs = new HashSet<>();
        if (!CollectionUtils.isEmpty(request.getSongIds())) {
            List<Song> foundSongs = songRepository.findAllById(request.getSongIds());
            if (foundSongs.size() != request.getSongIds().size()) {
                throw new ResourceNotFoundException("Một hoặc nhiều bài hát không tồn tại.");
            }

            for (Song song : foundSongs) {
                if (song.getStatus() != Song.SongStatus.APPROVED) {
                    throw new BadRequestException("Chỉ có thể thêm vào playlist các bài hát đã được duyệt.");
                }
                songs.add(song);
            }
        }

        Playlist playlist = Playlist.builder()
                .name(request.getName())
                .thumbnailPath(thumbnailPath)
                .songs(songs)
                .creator(isAdmin ? null : currentUser)
                .visibility(isAdmin ? PlaylistVisibility.PUBLIC : PlaylistVisibility.PRIVATE)
                .build();

        Playlist savedPlaylist = playlistRepository.save(playlist);

        return playlistMapper.toDto(savedPlaylist, currentUser);
    }

}