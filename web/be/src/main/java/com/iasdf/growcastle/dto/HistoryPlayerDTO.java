package com.iasdf.growcastle.dto;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iasdf.growcastle.domain.HistoryPlayer;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HistoryPlayerDTO
{
    private String name;
    @JsonProperty(value="parse_time")
    private LocalDateTime parseTime;

    private int score;
    private int rank;

    private int wave;
    @JsonProperty(value="horn_jump")
    private int hornJump;
    @JsonProperty(value="double_horn_jump")
    private int doubleHornJump;
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
