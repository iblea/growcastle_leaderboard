package com.iasdf.growcastle.service;

import java.util.List;

import com.iasdf.growcastle.dto.LeaderboardPlayerDTO;
import com.iasdf.growcastle.repository.PlayerLeaderboardRepository;

public class PlayerLeaderboardService {

    private final PlayerLeaderboardRepository playerRepository;

    public PlayerLeaderboardService(PlayerLeaderboardRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /*
     * 전체 플레이어 조회
     */
    public List<LeaderboardPlayerDTO> findAllPlayers() {
        return LeaderboardPlayerDTO.toDTO(
            playerRepository.findAll()
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
            playerRepository.findsOffset(limit, (page - 1) * limit)
        );
    }

}