package com.iasdf.growcastle.service;

import org.springframework.stereotype.Service;

import com.iasdf.growcastle.dto.HistoryDTO;
import com.iasdf.growcastle.repository.PlayerHistoryRepository;

@Service
public class PlayerHistoryService {

    private final PlayerHistoryRepository playerHistoryRepository;

    public PlayerHistoryService(PlayerHistoryRepository playerHistoryRepository) {
        this.playerHistoryRepository = playerHistoryRepository;
    }

    public HistoryDTO findPlayer(String name) {
        return HistoryDTO.toDTO(
            playerHistoryRepository.findPlayerInfo(name, 60)
        );
    }

    public HistoryDTO findPlayerHistory(String name) {
        return HistoryDTO.toDTO(
            playerHistoryRepository.findPlayerHistory(name, 60)
        );
    }

}