package com.iasdf.growcastle.dto;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import com.iasdf.growcastle.domain.LeaderboardPlayer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(title = "LeaderboardDetailData", description = "Leaderboard Player DTO")
public class LeaderboardDetailDataDTO
{
    @NotBlank(message = "User / Guild Name")
    @Size(min = 1, max = 20, message = "이름은 1자 이상 20자 이하로 입력해주세요.")
    @Schema(description = "Player Name", example = "Ib")
    @Pattern(regexp = "^[a-zA-Z0-9 _-]+$", message = "영문 대소문자, 숫자, 공백, '_', '-'만 입력 가능합니다.")
    private String name;

    @Schema(description = "Player Score", example = "100")
    private int score;

    @Schema(description = "Player Rank", example = "1")
    private int rank;

    // @Schema(description = "해당 데이터가 파싱된 시각", example = "2021-09-01T00:00:00")
    // @JsonProperty(value="parse_time")
    // private LocalDateTime parseTime;

    // @Schema(description = "Wave 클리어 횟수", example = "1")
    // private int wave;
    // @Schema(description = "Horn Jump 횟수", example = "1")
    // @JsonProperty(value="horn_jump")
    // private int hornJump;
    // @Schema(description = "Double Horn Jump 횟수", example = "1")
    // @JsonProperty(value="double_horn_jump")
    // private int doubleHornJump;
    // @Schema(description = "Crystal Jump 횟수", example = "1")
    // @JsonProperty(value="crystal_jump")
    // private int crystalJump;


    public LeaderboardDetailDataDTO(LeaderboardPlayer player) {
        this.name = player.getMemberPK().getName();
        this.score = player.getScore();
        this.rank = player.getRank();
        // this.parseTime = player.getMemberPK().getParseTime();
        // this.wave = player.getWave();
        // this.hornJump = player.getHornJump();
        // this.doubleHornJump = player.getDhornJump();
        // this.crystalJump = player.getCrystalJump();
    }

    public LeaderboardDetailDataDTO(String name, int score, int rank) {
        this.name = name;
        this.score = score;
        this.rank = rank;
    }

    public static List<LeaderboardDetailDataDTO> toDTO(List<LeaderboardPlayer> players) {
        if (players == null) {
            return null;
        }
        List<LeaderboardDetailDataDTO> list = new LinkedList<>();

        for (LeaderboardPlayer player : players) {
            list.add(new LeaderboardDetailDataDTO(player));
        }
        return list;
    }

    public static LeaderboardDetailDataDTO toDTO(LeaderboardPlayer player) {
        if (player == null) {
            return null;
        }
        return new LeaderboardDetailDataDTO(player);
    }
}