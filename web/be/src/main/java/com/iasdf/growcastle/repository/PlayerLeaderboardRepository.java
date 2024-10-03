package com.iasdf.growcastle.repository;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.iasdf.growcastle.domain.Player;

public class PlayerLeaderboardRepository {

    public List<Player> findAll() {
        return new LinkedList<>();
    }

    public List<Player> finds(int limit) {
        if (limit < 0) {
            return findAll();
        }
        return new LinkedList<>();
    }

}