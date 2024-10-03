package com.iasdf.growcastle.service;

import java.util.List;

import com.iasdf.growcastle.domain.Player;
import com.iasdf.growcastle.domain.Players;
import com.iasdf.growcastle.repository.PlayerRepository;

public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /*
     * 전체 플레이어 조회
     */
    public List<Player> findAllPlayers() {
        return playerRepository.findAll();
        // List<Player> listPlayers = playerRepository.findAll();
        // return new Players(listPlayers);
    }

    /*
     * 전체 플레이어 조회
     */
    public List<Player> findPlayers(int limit) {
        return null;
        // return playerRepository.finds(limit);
        // List<Player> listPlayers = playerRepository.findAll();
        // return new Players(listPlayers);
    }

}