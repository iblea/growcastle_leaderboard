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
import com.iasdf.growcastle.dto.HistoryDTO;
import com.iasdf.growcastle.service.PlayerHistoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Player History", description = "Player History API")
@Controller
public class PlayerHistoryController {

    private final PlayerHistoryService playerHistoryService;

    @Autowired
    public PlayerHistoryController(PlayerHistoryService playerHistoryService) {
        this.playerHistoryService = playerHistoryService;
    }

    @GetMapping("/player/history/{name}")
    @Operation(summary = "Get Player Leaderboard", description = "Get Player Leaderboard Top 200")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Success",
            content = { @Content(schema = @Schema(implementation = HistoryDTO.class ))}),
        @ApiResponse(responseCode = "400", description = "Bad Request (Argument Error)"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error (Server / Database Error or etc.)")
    })
    public ResponseEntity<Object> findLeaderboards(
        @PathVariable String name
        // @RequestParam(name = "unit", required = false, defaultValue = "60") Integer unit
    ) throws SQLDataException {
        ArgChecker.isValidUserName(name);
        // ArgChecker.isValidUnit(unit);

        // HistoryPlayerDTO player = playerHistoryService.findPlayer(name);
        HistoryDTO player = playerHistoryService.findPlayerHistory(name);
        if (player == null) {
            throw new SQLDataException("Player History Data Search Error");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("success", 1);
        response.put("data", player);

        return ResponseEntity.ok(response);
    }

}
