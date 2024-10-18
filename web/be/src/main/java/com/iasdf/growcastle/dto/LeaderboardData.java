package com.iasdf.growcastle.dto;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import com.iasdf.growcastle.common.TimeUtil;
import com.iasdf.growcastle.domain.LeaderboardPlayer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(title = "LeaderboardData", description = "Leaderboard Player DTO")
public class LeaderboardData {

    @Schema(description = "성공 여부", example = "true")
    private Boolean success;
    @Schema(description = "해당 데이터가 파싱된 시각", example = "2021-09-01T00:00:00")
    private LocalDateTime parseTime;
    @Schema(description = "Leaderboard Type", example = "player / guild")
    private String type;
    @Schema(description = "데이터 개수", example = "10")
    private int cnt;
    @Schema(description = "Leaderboard Player / Guild Data")
    private List<LeaderboardDetailDataDTO> data;


    public LeaderboardData() {
        this.success = false;
        this.cnt = 0;
        this.parseTime = TimeUtil.getNow();
        this.type = "";
        this.data = null;
    }

    public LeaderboardData(String type) {
        this.success = true;
        this.parseTime = TimeUtil.getNow();
        this.type = type;
        this.data = new LinkedList<>();
        this.cnt = 0;
    }


    public LeaderboardData(LocalDateTime parseTime, String type, List<LeaderboardDetailDataDTO> data) {
        this.parseTime = parseTime;
        this.type = type;
        this.data = data;
        if (this.data == null) {
            this.success = false;
            this.cnt = 0;
        } else {
            this.success = true;
            this.cnt = this.data.size();
        }
    }


    public static LeaderboardData toDTOPlayer(List<LeaderboardPlayer> players) {
        return toDTO("player", players);
    }

    public static LeaderboardData toDTOGuild(List<LeaderboardPlayer> players) {
        return toDTO("guild", players);
    }


    private static LeaderboardData toDTO(String type, List<LeaderboardPlayer> players) {
        if (players == null) {
            return null;
        }
        if (players.isEmpty()) {
            return new LeaderboardData(type);
        }

        List<LeaderboardDetailDataDTO> playerDTOs = LeaderboardDetailDataDTO.toDTO(players);
        if (playerDTOs == null) {
            return null;
        }

        LocalDateTime time = players.get(0).getMemberPK().getParseTime();
        return new LeaderboardData(time, type, playerDTOs);
    }

}