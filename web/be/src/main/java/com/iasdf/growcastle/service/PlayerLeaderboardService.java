package com.iasdf.growcastle.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iasdf.growcastle.dto.LeaderboardPlayerDTO;
import com.iasdf.growcastle.repository.PlayerLeaderboardRepository;

@Service
public class PlayerLeaderboardService {

    private final PlayerLeaderboardRepository playerLeaderboardRepository;

    @Autowired
    public PlayerLeaderboardService(PlayerLeaderboardRepository playerRepository) {
        this.playerLeaderboardRepository = playerRepository;
    }

    /*
     * 전체 플레이어 조회
     */
    public List<LeaderboardPlayerDTO> findAllPlayers() {
        return LeaderboardPlayerDTO.toDTO(
            playerLeaderboardRepository.findAll()
        );
    }

    /*
     * 전체 플레이어 조회
     */
    // 10, 20, 50, 100, 200
    public List<LeaderboardPlayerDTO> findPlayers(int limit, int page) {
        if (limit < 0) {
            return null;
        }
        if (page <= 0) {
            return null;
        }
        if (limit == 0 || limit == 200) {
            return findAllPlayers();
        }

        return LeaderboardPlayerDTO.toDTO(
            playerLeaderboardRepository.findsOffset(limit, (page - 1) * limit)
        );
    }

}