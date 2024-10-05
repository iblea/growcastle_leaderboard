package com.iasdf.growcastle.controller;

import java.sql.SQLDataException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.iasdf.growcastle.common.ArgChecker;
import com.iasdf.growcastle.dto.HistoryPlayerDTO;
import com.iasdf.growcastle.service.PlayerHistoryService;

@Controller
public class PlayerHistoryController {

    private final PlayerHistoryService playerHistoryService;

    @Autowired
    public PlayerHistoryController(PlayerHistoryService playerHistoryService) {
        this.playerHistoryService = playerHistoryService;
    }

    @GetMapping("/player/history/{name}")
    public ResponseEntity<Object> findLeaderboards(
        @PathVariable String name,
        @RequestParam(name = "unit", required = false, defaultValue = "60") Integer unit
    ) throws SQLDataException {
        ArgChecker.isValidUserName(name);
        ArgChecker.isValidUnit(unit);

        HistoryPlayerDTO player = playerHistoryService.findPlayer(name);
        if (player == null) {
            throw new SQLDataException("Player History Data Search Error");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("success", 1);
        response.put("data", null);

        return ResponseEntity.ok("test");
    }

}
