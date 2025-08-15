package com.musicapp.backend.service;

import com.musicapp.backend.dto.playlist.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final FileStorageService fileStorageService;
    private final SongRepository songRepository;
    private final PlaylistMapper playlistMapper;

    @Transactional
    public PlaylistDto createPlaylist(CreatePlaylistRequest request, MultipartFile thumbnailFile, User currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isCreator = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CREATOR"));

        String thumbnailPath = null;
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            thumbnailPath = fileStorageService.storeFile(thumbnailFile, "images/playlists");
        }

        Set<Song> songs = processSongIdsForPlaylist(request.getSongIds(), currentUser, isAdmin, isCreator);

        Playlist playlist = Playlist.builder()
                .name(request.getName())
                .thumbnailPath(thumbnailPath)
                .songs(songs)
                .creator(currentUser)
                .visibility((isAdmin || isCreator) ? PlaylistVisibility.PUBLIC : PlaylistVisibility.PRIVATE)
                .build();

        Playlist savedPlaylist = playlistRepository.save(playlist);

        return playlistMapper.toDto(savedPlaylist, currentUser);
    }

    public PlaylistDetailDto getPlaylistById(Long playlistId, User currentUser) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        PlaylistVisibility visibility = playlist.getVisibility();

        if (visibility == PlaylistVisibility.PRIVATE) {
            if (currentUser == null) {
                throw new UnauthorizedException("Bạn phải đăng nhập để xem playlist này.");
            }
            boolean isOwner = playlist.getCreator() != null &&
                    playlist.getCreator().getId().equals(currentUser.getId());
            if (!isOwner) {
                throw new UnauthorizedException("Bạn không có quyền xem playlist này.");
            }
        }
        else if (visibility == PlaylistVisibility.HIDDEN) {
            if (currentUser == null) {
                throw new UnauthorizedException("Bạn phải đăng nhập để xem playlist này.");
            }

            boolean isOwner = playlist.getCreator() != null &&
                    playlist.getCreator().getId().equals(currentUser.getId());
            boolean isAdmin = currentUser.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isOwner && !isAdmin) {
                throw new UnauthorizedException("Bạn không có quyền xem playlist này.");
            }
        }

        return playlistMapper.toDetailDto(playlist, currentUser);
    }


    public List<PlaylistDto> getMyPlaylists(User currentUser) {
        List<Playlist> playlists = playlistRepository.findByCreatorId(currentUser.getId());
        return playlists.stream()
                .map(p -> playlistMapper.toDto(p, currentUser))
                .collect(Collectors.toList());
    }

    public AdminPlaylistManagementDto getPlaylistsForAdminManagement(User admin) {
        List<PlaylistDto> adminPlaylists = getMyPlaylists(admin);

        List<Playlist> creatorPlaylistsRaw = playlistRepository.findPlaylistsByCreators();
        List<PlaylistDto> creatorPlaylists = creatorPlaylistsRaw.stream()
                .map(p -> playlistMapper.toDto(p, admin))
                .collect(Collectors.toList());

        return AdminPlaylistManagementDto.builder()
                .adminPlaylists(adminPlaylists)
                .creatorPlaylists(creatorPlaylists)
                .build();
    }

    @Transactional
    public PlaylistDetailDto updatePlaylist(
            Long playlistId,
            UpdatePlaylistRequest request,
            MultipartFile thumbnailFile,
            User currentUser
    ) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        boolean isOwner = (playlist.getCreator() != null && playlist.getCreator().getId().equals(currentUser.getId()));
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
            boolean isAdmin = currentUser.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isCreator = currentUser.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CREATOR"));

            Set<Song> newSongs = processSongIdsForPlaylist(request.getSongIds(), currentUser, isAdmin, isCreator);
            playlist.setSongs(newSongs);
        }
        Playlist updatedPlaylist = playlistRepository.save(playlist);
        return playlistMapper.toDetailDto(updatedPlaylist, currentUser);
    }

    @Transactional
    public void deletePlaylist(Long playlistId, User currentUser) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isOwner = playlist.getCreator() != null &&
                playlist.getCreator().getId().equals(currentUser.getId());

        User playlistCreator = playlist.getCreator();
        boolean wasCreatedByCreator = playlistCreator != null && playlistCreator.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CREATOR"));

        boolean canDelete = isOwner || (isAdmin && wasCreatedByCreator);

        if (!canDelete) {
            throw new UnauthorizedException("Bạn không có quyền xóa playlist này.");
        }

        playlistRepository.delete(playlist);
    }

    @Transactional
    public PlaylistDto addSongsToPlaylist(Long playlistId, AddSongsToPlaylistRequest request, User currentUser) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        boolean isOwner = (playlist.getCreator() != null && playlist.getCreator().getId().equals(currentUser.getId()));
        if (!isOwner) {
            throw new UnauthorizedException("Bạn không có quyền thêm bài hát vào playlist này.");
        }

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isCreator = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CREATOR"));

        Set<Song> songsToAdd = processSongIdsForPlaylist(request.getSongIds(), currentUser, isAdmin, isCreator);

        playlist.getSongs().addAll(songsToAdd);

        Playlist updatedPlaylist = playlistRepository.save(playlist);
        return playlistMapper.toDto(updatedPlaylist, currentUser);
    }

    @Transactional
    public PlaylistDto removeSongFromPlaylist(Long playlistId, Long songId, User currentUser) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        boolean isOwner = (playlist.getCreator() != null && playlist.getCreator().getId().equals(currentUser.getId()));

        if (!isOwner) {
            throw new UnauthorizedException("Bạn không có quyền xóa bài hát khỏi playlist này.");
        }

        Song songToRemove = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài hát với ID: " + songId + " để xóa."));

        boolean removed = playlist.getSongs().remove(songToRemove);

        if (!removed) {
            throw new ResourceNotFoundException("Bài hát với ID: " + songId + " không có trong playlist này.");
        }

        Playlist updatedPlaylist = playlistRepository.save(playlist);
        return playlistMapper.toDto(updatedPlaylist, currentUser);
    }

    private Set<Song> processSongIdsForPlaylist(List<Long> songIds, User currentUser, boolean isAdmin, boolean isCreator) {
        Set<Song> processedSongs = new HashSet<>();
        if (CollectionUtils.isEmpty(songIds)) {
            return processedSongs;
        }

        // --- BẮT ĐẦU SỬA ĐỔI ---
        List<Song> foundSongs = songRepository.findByIdInWithCreator(songIds);
        // --- KẾT THÚC SỬA ĐỔI ---

        if (foundSongs.size() != songIds.size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều bài hát không tồn tại.");
        }

        for (Song song : foundSongs) {
            if (song.getStatus() != Song.SongStatus.APPROVED) {
                throw new BadRequestException("Chỉ có thể thêm vào playlist các bài hát đã được duyệt (ID: " + song.getId() + ").");
            }

            if (isCreator && !isAdmin) {
                if (song.getCreator() == null || !song.getCreator().getId().equals(currentUser.getId())) {
                    throw new UnauthorizedException("Creator chỉ có thể thêm bài hát do chính mình tạo (ID: " + song.getId() + ").");
                }
            }
            processedSongs.add(song);
        }
        return processedSongs;
    }

    @Transactional
    public PlaylistDto togglePlaylistVisibility(Long playlistId, User currentUser) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        if (playlist.getVisibility() == PlaylistVisibility.PRIVATE) {
            throw new BadRequestException("Không thể ẩn/hiện playlist cá nhân (PRIVATE).");
        }

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = playlist.getCreator() != null &&
                playlist.getCreator().getId().equals(currentUser.getId());
        User playlistCreator = playlist.getCreator();
        boolean wasCreatedByCreator = playlistCreator != null && playlistCreator.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CREATOR"));

        boolean canToggle = isOwner || (isAdmin && wasCreatedByCreator);

        if (!canToggle) {
            throw new UnauthorizedException("Bạn không có quyền thay đổi trạng thái của playlist này.");
        }

        if (playlist.getVisibility() == PlaylistVisibility.PUBLIC) {
            playlist.setVisibility(PlaylistVisibility.HIDDEN);
        } else if (playlist.getVisibility() == PlaylistVisibility.HIDDEN) {
            playlist.setVisibility(PlaylistVisibility.PUBLIC);
        }

        Playlist updatedPlaylist = playlistRepository.save(playlist);
        return playlistMapper.toDto(updatedPlaylist, currentUser);
    }

    public Page<PlaylistDto> getAllPublicPlaylists(Pageable pageable, User currentUser) {
        Page<Playlist> playlistPage = playlistRepository.findByVisibilityOrderByListenCountDesc(Playlist.PlaylistVisibility.PUBLIC, pageable);
        return playlistPage.map(p -> playlistMapper.toDto(p, currentUser));
    }

    @Transactional
    public void incrementListenCount(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId));

        if (playlist.getVisibility() == PlaylistVisibility.PUBLIC) {
            playlistRepository.incrementListenCount(playlistId);
        }
    }

    public List<PlaylistDto> getTopListenedPlaylists(int limit, User currentUser) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Playlist> playlists = playlistRepository.findTopListenedPublicPlaylists(pageable);
        return playlists.stream()
                .map(p -> playlistMapper.toDto(p, currentUser))
                .collect(Collectors.toList());
    }
}