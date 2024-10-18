package com.iasdf.growcastle.controller;

import java.sql.SQLDataException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.iasdf.growcastle.common.ArgChecker;
import com.iasdf.growcastle.dto.LeaderboardData;
import com.iasdf.growcastle.service.PlayerLeaderboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Player Leaderboard", description = "Player Leaderboard API")
@Controller
public class PlayerLeaderboardController {

    // @Autowired
    private final PlayerLeaderboardService playerLeaderboardService;

    public PlayerLeaderboardController(PlayerLeaderboardService playerLeaderboardService) {
        this.playerLeaderboardService = playerLeaderboardService;
    }

    @GetMapping("/player/leaderboard")
    @Operation(summary = "Get Player Leaderboard", description = "Get Player Leaderboard Top 200")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Success",
            content = { @Content(schema = @Schema(implementation = LeaderboardData.class ))}),
        @ApiResponse(responseCode = "400", description = "Bad Request (Argument Error)"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error (Server / Database Error or etc.)")
    })
    public ResponseEntity<Object> leaderboards(
        @RequestParam(name = "cnt", required = false, defaultValue = "0") Integer cnt,
        @RequestParam(name = "page", required = false, defaultValue = "1") Integer page
    ) throws SQLDataException {
        // ArgChecker.isValidUserName(name);
        int showCnt = cnt;
        ArgChecker.isValidCnt(showCnt);
        ArgChecker.isValidPage(page);

        LeaderboardData players = playerLeaderboardService.findPlayers(showCnt, page);
        if (players == null) {
            throw new SQLDataException("Player Leaderboard Data Search Error");
        }
        return ResponseEntity.ok(players);
    }

}
