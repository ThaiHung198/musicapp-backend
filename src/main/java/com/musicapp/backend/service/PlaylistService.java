package com.musicapp.backend.service;

import com.musicapp.backend.dto.playlist.AddSongsToPlaylistRequest;
import com.musicapp.backend.dto.playlist.CreatePlaylistRequest;
import com.musicapp.backend.dto.playlist.PlaylistDto;
import com.musicapp.backend.dto.playlist.UpdatePlaylistRequest;
import com.musicapp.backend.entity.Playlist;
import com.musicapp.backend.entity.Playlist.PlaylistVisibility;
import com.musicapp.backend.entity.Song;
import com.musicapp.backend.entity.User;

import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceNotFoundException;
import com.musicapp.backend.exception.UnauthorizedException;

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
    private final FileStorageService fileStorageService;
    private final SongRepository songRepository;
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

                .songs(new HashSet<>())
                .likes(new HashSet<>())
                .creator(isAdmin ? null : currentUser)

                .visibility(isAdmin ? PlaylistVisibility.PUBLIC : PlaylistVisibility.PRIVATE)
                .build();

        Playlist savedPlaylist = playlistRepository.save(playlist);

        // Giả sử playlistMapper đã được cập nhật để không cần truy cập playlist.getComments()
        // Nếu playlistMapper cũng lỗi, chúng ta cần sửa nó tiếp theo.
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

    @Transactional
    public PlaylistDto updatePlaylist(
            Long playlistId,
            UpdatePlaylistRequest request,
            MultipartFile thumbnailFile,
            User currentUser
    ) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        boolean isOwner = (playlist.getCreator() != null && playlist.getCreator().getId().equals(currentUser.getId())) ||
                (playlist.getCreator() == null && currentUser.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        if (!isOwner) {
            throw new UnauthorizedException("Bạn không có quyền sửa playlist này.");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            playlist.setName(request.getName());
        }

        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            String newThumbnailPath = fileStorageService.storeFile(thumbnailFile, "images/playlists");
            playlist.setThumbnailPath(newThumbnailPath);
        }

        if (request.getSongIds() != null) {
            Set<Song> newSongs = new HashSet<>();
            if (!request.getSongIds().isEmpty()) {
                List<Song> foundSongs = songRepository.findAllById(request.getSongIds());
                if (foundSongs.size() != request.getSongIds().size()) {
                    throw new ResourceNotFoundException("Một hoặc nhiều bài hát không tồn tại.");
                }
                for (Song song : foundSongs) {
                    if (song.getStatus() != Song.SongStatus.APPROVED) {
                        throw new BadRequestException("Chỉ có thể thêm vào playlist các bài hát đã được duyệt.");
                    }
                    newSongs.add(song);
                }
            }
            playlist.setSongs(newSongs);
        }

        Playlist updatedPlaylist = playlistRepository.save(playlist);

        return playlistMapper.toDto(updatedPlaylist, currentUser);
    }


    @Transactional
    public void deletePlaylist(Long playlistId, User currentUser) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        boolean isOwner = playlist.getCreator() != null &&
                playlist.getCreator().getId().equals(currentUser.getId());

        if (!isOwner) {
            throw new UnauthorizedException("Bạn không có quyền xóa playlist này.");
        }

        playlistRepository.delete(playlist);
    }

    @Transactional
    public PlaylistDto addSongsToPlaylist(Long playlistId, AddSongsToPlaylistRequest request, User currentUser) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        boolean isOwner = (playlist.getCreator() != null && playlist.getCreator().getId().equals(currentUser.getId())) ||
                (playlist.getCreator() == null && currentUser.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        if (!isOwner) {
            throw new UnauthorizedException("Bạn không có quyền thêm bài hát vào playlist này.");
        }

        List<Song> songsToAdd = songRepository.findAllById(request.getSongIds());
        if (songsToAdd.size() != request.getSongIds().size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều bài hát trong danh sách không tồn tại.");
        }

        int songsAddedCount = 0;
        for (Song song : songsToAdd) {
            if (song.getStatus() != Song.SongStatus.APPROVED) {
                continue;
            }
            if (playlist.getSongs().add(song)) {
                songsAddedCount++;
            }
        }
        return playlistMapper.toDto(playlist, currentUser);
    }

    @Transactional
    public PlaylistDto removeSongFromPlaylist(Long playlistId, Long songId, User currentUser) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        boolean isOwner = (playlist.getCreator() != null && playlist.getCreator().getId().equals(currentUser.getId())) ||
                (playlist.getCreator() == null && currentUser.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        if (!isOwner) {
            throw new UnauthorizedException("Bạn không có quyền xóa bài hát khỏi playlist này.");
        }

        Song songToRemove = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài hát với ID: " + songId + " để xóa."));

        boolean removed = playlist.getSongs().remove(songToRemove);

        if (!removed) {
            throw new ResourceNotFoundException("Bài hát với ID: " + songId + " không có trong playlist này.");
        }

        return playlistMapper.toDto(playlist, currentUser);
    }
}