// backend/src/main/java/com/musicapp/backend/service/ChartService.java

package com.musicapp.backend.service;

import com.musicapp.backend.dto.ChartSongDto;
import com.musicapp.backend.entity.ChartRanking;
import com.musicapp.backend.entity.Like;
import com.musicapp.backend.entity.Song;
import com.musicapp.backend.entity.User;
import com.musicapp.backend.mapper.SongMapper;
import com.musicapp.backend.repository.ChartRankingRepository;
import com.musicapp.backend.repository.ListenHistoryRepository;
import com.musicapp.backend.repository.LikeRepository;
import com.musicapp.backend.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartService {

    private final SongRepository songRepository;
    private final ListenHistoryRepository listenHistoryRepository;
    private final LikeRepository likeRepository;
    private final ChartRankingRepository chartRankingRepository;
    private final SongMapper songMapper;

    private static final double LISTEN_24H_WEIGHT = 5.0;
    private static final double LISTEN_7D_WEIGHT = 2.0;
    private static final double LIKE_WEIGHT = 1.0;
    private static final double NEW_SONG_WEIGHT = 0.5;
    private static final long NEW_SONG_DAYS = 7;

    @Transactional(readOnly = true)
    public Page<ChartSongDto> getChart(Pageable pageable, User currentUser) {
        Page<ChartRanking> rankingsPage = chartRankingRepository.findAllWithSongDetails(pageable);
        return rankingsPage.map(ranking -> {
            ChartSongDto dto = new ChartSongDto();
            dto.setRank(ranking.getCurrentRank());
            dto.setPreviousRank(ranking.getPreviousRank());
            dto.setSong(songMapper.toDto(ranking.getSong(), currentUser));
            return dto;
        });
    }

    @Scheduled(cron = "0 0 * * * *") // Chạy mỗi giờ
    @Transactional
    public void calculateAndSaveChart() {
        log.info("Starting chart calculation job at {}", LocalDateTime.now());

        List<Song> allApprovedSongs = songRepository.findByStatusOrderByTitleAsc(Song.SongStatus.APPROVED);
        LocalDateTime now = LocalDateTime.now();

        Map<Long, Integer> previousRanks = chartRankingRepository.findAll().stream()
                .collect(Collectors.toMap(r -> r.getSong().getId(), ChartRanking::getCurrentRank));

        List<ChartRanking> newRankings = allApprovedSongs.stream()
                .map(song -> {
                    double score = calculateTrendingScore(song, now);
                    ChartRanking ranking = new ChartRanking();
                    ranking.setSong(song);
                    ranking.setScore(score);
                    ranking.setPreviousRank(previousRanks.getOrDefault(song.getId(), null));
                    ranking.setUpdatedAt(now);
                    return ranking;
                })
                .sorted(Comparator.comparingDouble(ChartRanking::getScore).reversed())
                .collect(Collectors.toList());

        chartRankingRepository.deleteAllInBatch();

        for (int i = 0; i < newRankings.size(); i++) {
            if (i >= 100) break;
            ChartRanking ranking = newRankings.get(i);
            ranking.setCurrentRank(i + 1);
            chartRankingRepository.save(ranking);
        }

        log.info("Finished chart calculation job. Processed and saved top {} songs.", Math.min(100, newRankings.size()));
    }

    private double calculateTrendingScore(Song song, LocalDateTime now) {
        long listens24h = listenHistoryRepository.countListensForSongSince(song.getId(), now.minusHours(24));
        long listens7d = listenHistoryRepository.countListensForSongSince(song.getId(), now.minusDays(7));
        long totalLikes = likeRepository.countByLikeableIdAndLikeableType(song.getId(), Like.LikeableType.SONG);

        double newSongBonus = 0;
        if (ChronoUnit.DAYS.between(song.getCreatedAt(), now) <= NEW_SONG_DAYS) {
            newSongBonus = 100;
        }

        return (listens24h * LISTEN_24H_WEIGHT) +
                (listens7d * LISTEN_7D_WEIGHT) +
                (totalLikes * LIKE_WEIGHT) +
                (newSongBonus * NEW_SONG_WEIGHT);
    }
}