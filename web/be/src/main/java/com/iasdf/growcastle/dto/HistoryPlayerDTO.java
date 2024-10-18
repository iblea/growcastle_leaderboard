package com.iasdf.growcastle.dto;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iasdf.growcastle.domain.HistoryPlayer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(title = "HistoryDetailData", description = "History Player DTO")
public class HistoryPlayerDTO
{
    @NotBlank(message = "User Name")
    @Size(min = 1, max = 20, message = "이름은 1자 이상 20자 이하로 입력해주세요.")
    @Schema(description = "Player Name", example = "Ib")
    @Pattern(regexp = "^[a-zA-Z0-9 _-]+$", message = "영문 대소문자, 숫자, 공백, '_', '-'만 입력 가능합니다.")
    private String name;

    @JsonProperty(value="parse_time")
    @Schema(description = "웨이빙이 기록된 시간 텀", example = "2021-09-01T00:00:00")
    private LocalDateTime parseTime;

    @Schema(description = "Player Score", example = "100")
    private int score;

    @Schema(description = "Player Rank", example = "1")
    private int rank;

    @Schema(description = "Wave 클리어 횟수", example = "1")
    private int wave;
    @Schema(description = "Horn Jump 횟수", example = "1")
    @JsonProperty(value="horn_jump")
    private int hornJump;
    @Schema(description = "Double Horn Jump 횟수", example = "1")
    @JsonProperty(value="double_horn_jump")
    private int doubleHornJump;
    @Schema(description = "Crystal Jump 횟수", example = "1")
    @JsonProperty(value="crystal_jump")
    private int crystalJump;

    public HistoryPlayerDTO() {}

    public HistoryPlayerDTO(HistoryPlayer historyPlayer)
    {
        this.name = historyPlayer.getMemberPK().getName();
        this.parseTime = historyPlayer.getMemberPK().getParseTime();
        this.score = historyPlayer.getScore();
        this.rank = historyPlayer.getRank();
        this.wave = historyPlayer.getWave();
        this.hornJump = historyPlayer.getHornJump();
        this.doubleHornJump = historyPlayer.getDhornJump();
        this.crystalJump = historyPlayer.getCrystalJump();
    }

    public HistoryPlayerDTO(String name, LocalDateTime parseTime, int score, int rank, int wave, int hornJump, int doubleHornJump, int crystalJump)
    {
        this.name = name;
        this.parseTime = parseTime;
        this.score = score;
        this.rank = rank;
        this.wave = wave;
        this.hornJump = hornJump;
        this.doubleHornJump = doubleHornJump;
        this.crystalJump = crystalJump;
    }

    public static List<HistoryPlayerDTO> toDTO(List<HistoryPlayer> players) {
        if (players == null) {
            return null;
        }
        List<HistoryPlayerDTO> list = new LinkedList<>();

        for (HistoryPlayer player : players) {
            list.add(new HistoryPlayerDTO(player));
        }
        return list;
    }

    public static HistoryPlayerDTO toDTO(HistoryPlayer player) {
        if (player == null) {
            return null;
        }
        return new HistoryPlayerDTO(player);
    }

}
