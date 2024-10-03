package com.iasdf.growcastle.controller;

import java.sql.SQLDataException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.iasdf.growcastle.common.ArgChecker;
import com.iasdf.growcastle.domain.Player;
import com.iasdf.growcastle.service.PlayerLeaderboardService;

@Controller
public class PlayerLeaderboardController {

    // @Autowired
    private final PlayerLeaderboardService playerLeaderboardService;

    public PlayerLeaderboardController(PlayerLeaderboardService playerLeaderboardService) {
        this.playerLeaderboardService = playerLeaderboardService;
    }

    @GetMapping("/player/leaderboard")
    public ResponseEntity<Object> leaderboards(
        @RequestParam(name = "cnt", required = false, defaultValue = "0") Integer cnt
    ) throws SQLDataException {
        // ArgChecker.isValidUserName(name);
        int showCnt = cnt;
        ArgChecker.isValidCnt(showCnt);

        List<Player> players = playerLeaderboardService.findPlayers(showCnt);
        if (players == null) {
            throw new SQLDataException("Player Leaderboard Data Search Error");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", 1);
        response.put("data", players);

        return ResponseEntity.ok("test");
    }

    @GetMapping("/player/leaderboard/{name}")
    public ResponseEntity<Object> findLeaderboards(
        @PathVariable String name
        // @RequestParam(name = "name", required = false, defaultValue = "") String name
    ) {
        ArgChecker.isValidUserName(name);
        Map<String, Object> response = new HashMap<>();
        response.put("success", 1);
        response.put("data", null);

        return ResponseEntity.ok("test");
    }

}
