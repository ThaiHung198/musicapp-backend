package com.musicapp.backend.service;

import com.musicapp.backend.dto.PagedResponse;
import com.musicapp.backend.dto.song.*;
import com.musicapp.backend.entity.*;
import com.musicapp.backend.exception.BadRequestException;
import com.musicapp.backend.exception.ResourceAlreadyExistsException;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final SingerRepository singerRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final SongMapper songMapper;
    private final FileStorageService fileStorageService;
    private final LikeRepository likeRepository;
    private final PlaylistRepository playlistRepository;
    private final ListenHistoryRepository listenHistoryRepository;


    @Transactional(readOnly = true)
    public List<SongDto> getAllSongsForPlaylist(User currentUser) {
        List<Song> songs;
        boolean isCreator = currentUser.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_CREATOR"));

        if (isCreator) {
            songs = songRepository.findByCreatorIdAndStatusOrderByTitleAsc(currentUser.getId(), Song.SongStatus.APPROVED);
        } else {
            songs = songRepository.findByStatusOrderByTitleAsc(Song.SongStatus.APPROVED);
        }

        return songs.stream()
                .map(song -> songMapper.toDto(song, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDto> searchApprovedSongsForPlaylist(Long playlistId, String keyword, User currentUser) {
        if (!playlistRepository.existsById(playlistId)) {
            throw new ResourceNotFoundException("Không tìm thấy playlist với ID: " + playlistId);
        }
        Pageable pageable = PageRequest.of(0, 20);
        Page<Song> songPage = songRepository.findApprovedSongsForPlaylist(playlistId, keyword, pageable);

        return songPage.stream()
                .map(song -> songMapper.toDto(song, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SongDto> getAllSongsForAdmin(String keyword, Pageable pageable, User admin) {
        Page<Song> songPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            songPage = songRepository.searchAllSongsByTitle(keyword.trim(), pageable);
        } else {
            songPage = songRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return songPage.map(song -> songMapper.toDto(song, admin));
    }

    @Transactional(readOnly = true)
    public Page<SongDto> getAllApprovedSongs(Pageable pageable, User currentUser) {
        return songRepository.findByStatusOrderByCreatedAtDesc(Song.SongStatus.APPROVED, pageable)
                .map(song -> songMapper.toDto(song, currentUser));
    }

    @Transactional
    public SongDto toggleSongVisibility(Long songId, User admin) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài hát với ID: " + songId));

        if (song.getStatus() == Song.SongStatus.APPROVED) {
            song.setStatus(Song.SongStatus.HIDDEN);
        } else if (song.getStatus() == Song.SongStatus.HIDDEN) {
            song.setStatus(Song.SongStatus.APPROVED);
        } else {
            throw new BadRequestException("Chỉ có thể ẩn/hiện các bài hát đã được duyệt (APPROVED) hoặc đang bị ẩn (HIDDEN).");
        }

        Song updatedSong = songRepository.save(song);
        return songMapper.toDto(updatedSong, admin);
    }

    @Transactional(readOnly = true)
    public Page<SongDto> searchSongs(String keyword, Pageable pageable, User currentUser) {
        return songRepository.searchApprovedSongs(keyword, Song.SongStatus.APPROVED, pageable)
                .map(song -> songMapper.toDto(song, currentUser));
    }

    @Transactional(readOnly = true)
    public SongDto getSongById(Long songId, User currentUser) {
        Song song = songRepository.findByIdAndStatus(songId, Song.SongStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + songId));
        return songMapper.toDto(song, currentUser);
    }

    @Transactional(readOnly = true)
    public SongDto getSongByIdForCreator(Long id, User creator) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + id));

        if (!song.getCreator().getId().equals(creator.getId()) &&
                !hasAdminRole(creator)) {
            throw new UnauthorizedException("You don't have permission to access this song");
        }

        return songMapper.toDto(song, creator);
    }

    @Transactional(readOnly = true)
    public Page<SongDto> getUserCreatedSongs(Long userId, Pageable pageable, User currentUser) {
        return songRepository.findByCreatorIdOrderByCreatedAtDesc(userId, pageable)
                .map(song -> songMapper.toDto(song, currentUser));
    }

    @Transactional(readOnly = true)
    public Page<SongDto> getSongsBySinger(Long singerId, Pageable pageable, User currentUser) {
        if (!singerRepository.existsById(singerId)) {
            throw new ResourceNotFoundException("Không tìm thấy ca sĩ với ID: " + singerId);
        }

        return songRepository.findBySingerIdAndApproved(singerId, pageable)
                .map(song -> songMapper.toDto(song, currentUser));
    }

    @Transactional(readOnly = true)
    public List<SongDto> getTopSongsByListenCount(int limit, User currentUser) {
        Pageable pageable = PageRequest.of(0, limit);
        return songRepository.findTopSongsByListenCount(pageable)
                .stream()
                .map(song -> songMapper.toDto(song, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDto> getRecentlyCreatedSongs(int limit, User currentUser) {
        Pageable pageable = PageRequest.of(0, limit);
        return songRepository.findRecentlyCreatedSongs(pageable)
                .stream()
                .map(song -> songMapper.toDto(song, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDto> getMostLikedSongs(int limit, User currentUser) {
        Pageable pageable = PageRequest.of(0, limit);

        List<Long> mostLikedSongIds = songRepository.findMostLikedAndApprovedSongIds(pageable);

        if (mostLikedSongIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Song> songs = songRepository.findAllById(mostLikedSongIds);

        songs.sort(Comparator.comparing(song -> mostLikedSongIds.indexOf(song.getId())));

        return songs.stream()
                .map(song -> songMapper.toDto(song, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDto> getRandomSongs(int limit, User currentUser) {
        List<Long> allApprovedSongIds = songRepository.findIdsByStatus(Song.SongStatus.APPROVED);

        if (allApprovedSongIds.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(allApprovedSongIds);

        int sublistSize = Math.min(limit, allApprovedSongIds.size());
        List<Long> randomSongIds = allApprovedSongIds.subList(0, sublistSize);

        List<Song> randomSongs = songRepository.findAllById(randomSongIds);

        return randomSongs.stream()
                .map(song -> songMapper.toDto(song, currentUser))
                .collect(Collectors.toList());
    }

    private String generateRandomHexColor() {
        Random random = new Random();
        int nextInt = random.nextInt(0xffffff + 1);
        return String.format("#%06x", nextInt);
    }

    @Transactional
    public SongDto createSongByAdmin(AdminCreateSongRequest request, MultipartFile audioFile, MultipartFile thumbnailFile, List<MultipartFile> newSingerAvatars, User admin) {
        if ((request.getSingerIds() == null || request.getSingerIds().isEmpty()) &&
                (request.getNewSingers() == null || request.getNewSingers().isEmpty())) {
            throw new BadRequestException("At least one existing singer or one new singer is required.");
        }

        String audioFilePath = fileStorageService.storeFile(audioFile, "audio");
        String thumbnailFilePath = (thumbnailFile != null && !thumbnailFile.isEmpty())
                ? fileStorageService.storeFile(thumbnailFile, "images/songs")
                : null;

        Set<Singer> singers = new HashSet<>();
        if (request.getSingerIds() != null && !request.getSingerIds().isEmpty()) {
            List<Singer> existingSingers = singerRepository.findAllById(request.getSingerIds());
            if (existingSingers.size() != request.getSingerIds().size()) {
                throw new ResourceNotFoundException("One or more existing singers not found.");
            }
            existingSingers.forEach(singer -> {
                if (singer.getStatus() != Singer.SingerStatus.APPROVED) {
                    throw new BadRequestException("Singer '" + singer.getName() + "' (ID: " + singer.getId() + ") is not approved yet.");
                }
                singers.add(singer);
            });
        }

        if (request.getNewSingers() != null && !request.getNewSingers().isEmpty()) {
            Map<String, MultipartFile> avatarFilesMap = (newSingerAvatars != null)
                    ? newSingerAvatars.stream().collect(Collectors.toMap(MultipartFile::getOriginalFilename, Function.identity(), (first, second) -> first))
                    : Collections.emptyMap();

            for (AdminCreateSongRequest.NewSingerInfo newSingerInfo : request.getNewSingers()) {
                if (StringUtils.hasText(newSingerInfo.getEmail()) && singerRepository.existsByEmail(newSingerInfo.getEmail())) {
                    throw new ResourceAlreadyExistsException("A singer with email '" + newSingerInfo.getEmail() + "' already exists.");
                }

                String avatarPath = null;
                if (StringUtils.hasText(newSingerInfo.getAvatarFileName()) && avatarFilesMap.containsKey(newSingerInfo.getAvatarFileName())) {
                    MultipartFile avatarFile = avatarFilesMap.get(newSingerInfo.getAvatarFileName());
                    avatarPath = fileStorageService.storeFile(avatarFile, "images/singers");
                }

                Singer newSinger = Singer.builder()
                        .name(newSingerInfo.getName())
                        .email(newSingerInfo.getEmail())
                        .avatarPath(avatarPath)
                        .status(Singer.SingerStatus.APPROVED)
                        .creator(admin)
                        .build();
                singers.add(singerRepository.save(newSinger));
            }
        }

        Set<Tag> tags = new HashSet<>();
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tags.addAll(tagRepository.findAllById(request.getTagIds()));
        }

        if (request.getNewTags() != null && !request.getNewTags().isEmpty()) {
            for (String tagName : request.getNewTags()) {
                Tag newTag = tagRepository.findByName(tagName).orElseGet(() -> {
                    Tag tag = new Tag();
                    tag.setName(tagName);
                    return tagRepository.save(tag);
                });
                tags.add(newTag);
            }
        }

        String description = request.getDescription();
        if (!StringUtils.hasText(description)) {
            description = "Bài hát này không có mô tả";
        }

        Song song = Song.builder()
                .title(request.getTitle())
                .description(description)
                .lyrics(request.getLyrics())
                .filePath(audioFilePath)
                .thumbnailPath(thumbnailFilePath)
                .creator(admin)
                .singers(singers)
                .tags(tags)
                .isPremium(request.isPremium())
                .status(Song.SongStatus.APPROVED)
                .color(generateRandomHexColor())
                .build();

        Song savedSong = songRepository.save(song);
        return songMapper.toDto(savedSong, admin);
    }


    @Transactional
    public SongDto updateSongByAdmin(Long songId, AdminUpdateSongRequest request, MultipartFile audioFile, MultipartFile thumbnailFile, User admin) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + songId));

        if (request.getTitle() != null) song.setTitle(request.getTitle());

        if (request.getDescription() != null) {
            if (StringUtils.hasText(request.getDescription())) {
                song.setDescription(request.getDescription());
            } else {
                song.setDescription("Bài hát này không có mô tả");
            }
        }

        if (request.getLyrics() != null) song.setLyrics(request.getLyrics());
        if (request.getIsPremium() != null) song.setIsPremium(request.getIsPremium());

        if (audioFile != null && !audioFile.isEmpty()) {
            String audioFilePath = fileStorageService.storeFile(audioFile, "audio");
            song.setFilePath(audioFilePath);
        }
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            String thumbnailFilePath = fileStorageService.storeFile(thumbnailFile, "images/songs");
            song.setThumbnailPath(thumbnailFilePath);
            song.setColor(generateRandomHexColor());
        }

        if (request.getSingerIds() != null) {
            Set<Singer> singers = new HashSet<>(singerRepository.findAllById(request.getSingerIds()));
            for (Singer singer : singers) {
                if (singer.getStatus() != Singer.SingerStatus.APPROVED) {
                    throw new BadRequestException("Singer '" + singer.getName() + "' is not approved yet.");
                }
            }
            song.setSingers(singers);
        }

        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            song.setTags(tags);
        }

        Song updatedSong = songRepository.save(song);
        return songMapper.toDto(updatedSong, admin);
    }

    @Transactional
    public SongDto createSong(CreateSongRequest request, User creator) {
        Set<Singer> singers = new HashSet<>();
        for (Long singerId : request.getSingerIds()) {
            Singer singer = singerRepository.findById(singerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Singer not found with id: " + singerId));
            singers.add(singer);
        }

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
                .isPremium(request.getIsPremium())
                .status(Song.SongStatus.PENDING)
                .color(generateRandomHexColor())
                .build();

        Song savedSong = songRepository.save(song);
        return songMapper.toDto(savedSong, creator);
    }

    @Transactional
    public SongDto updateSong(Long id, UpdateSongRequest request, User user) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + id));

        if (!song.getCreator().getId().equals(user.getId()) && !hasAdminRole(user)) {
            throw new UnauthorizedException("You don't have permission to update this song");
        }

        if (request.getTitle() != null) {
            song.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            song.setDescription(request.getDescription());
        }
        if (request.getThumbnailPath() != null) {
            song.setThumbnailPath(request.getThumbnailPath());
        }

        if (request.getSingerIds() != null) {
            Set<Singer> singers = new HashSet<>();
            for (Long singerId : request.getSingerIds()) {
                Singer singer = singerRepository.findById(singerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Singer not found with id: " + singerId));
                singers.add(singer);
            }
            song.setSingers(singers);
        }

        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>();
            for (Long tagId : request.getTagIds()) {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));
                tags.add(tag);
            }
            song.setTags(tags);
        }

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
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + id));

        songRepository.incrementListenCount(id);

        ListenHistory history = new ListenHistory();
        history.setSong(song);
        listenHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public Page<SongDto> getPendingSongs(Pageable pageable) {
        return songRepository.findByStatusOrderByCreatedAtDesc(Song.SongStatus.PENDING, pageable)
                .map(song -> songMapper.toDto(song, null));
    }

    @Transactional(readOnly = true)
    public boolean canUserAccessSong(Long songId, String username) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + songId));

        if (!song.getIsPremium()) {
            return true;
        }

        if (username == null) {
            return false;
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        return subscriptionService.hasActivePremiumSubscription(user.getId());
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public PagedResponse<SongDto> getMyLibrary(String username, String keyword, Pageable pageable) {
        User creator = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));

        Page<Song> songPage;

        if (StringUtils.hasText(keyword)) {
            songPage = songRepository.searchByTitleForCreatorAndStatus(
                    keyword,
                    creator.getId(),
                    Song.SongStatus.APPROVED,
                    pageable
            );
        } else {
            songPage = songRepository.findByCreatorIdAndStatusOrderByCreatedAtDesc(
                    creator.getId(),
                    Song.SongStatus.APPROVED,
                    pageable
            );
        }

        Page<SongDto> dtoPage = songPage.map(song -> songMapper.toDto(song, creator));

        return PagedResponse.of(dtoPage.getContent(), dtoPage);
    }

    @Transactional(readOnly = true)
    public List<LyricLineDto> getParsedLyrics(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + songId));

        String lrcText = song.getLyrics();
        if (lrcText == null || lrcText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<LyricLineDto> lyricLines = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})[.:](\\d{2,3})\\](.*)");

        for (String line : lrcText.split("\n")) {
            Matcher matcher = pattern.matcher(line.trim());
            if (matcher.matches()) {
                double minutes = Double.parseDouble(matcher.group(1));
                double seconds = Double.parseDouble(matcher.group(2));
                String millisStr = matcher.group(3);
                double millis = Double.parseDouble(millisStr);

                double totalTime = minutes * 60 + seconds + (millisStr.length() == 2 ? millis / 100.0 : millis / 1000.0);
                String text = matcher.group(4).trim();

                if (!text.isEmpty()) {
                    lyricLines.add(new LyricLineDto(totalTime, text));
                }
            }
        }
        return lyricLines;
    }
}