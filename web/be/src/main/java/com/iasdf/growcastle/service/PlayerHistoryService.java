package com.iasdf.growcastle.service;

import org.springframework.stereotype.Service;

import com.iasdf.growcastle.dto.HistoryPlayerDTO;
import com.iasdf.growcastle.repository.PlayerHistoryRepository;

@Service
public class PlayerHistoryService {

    private final PlayerHistoryRepository playerHistoryRepository;

    public PlayerHistoryService(PlayerHistoryRepository playerHistoryRepository) {
        this.playerHistoryRepository = playerHistoryRepository;
    }

    public HistoryPlayerDTO findPlayer(String name) {
        return HistoryPlayerDTO.toDTO(
            playerHistoryRepository.findPlayerInfo(name, 60)
        );
    }
}