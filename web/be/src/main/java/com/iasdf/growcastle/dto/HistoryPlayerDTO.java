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
    private int score;
    private int rank;
    @JsonProperty(value="parse_time")
    private LocalDateTime parseTime;

    private String season;
    @JsonProperty(value="min_unit")
    private int minUnit;

    private int wave;
    @JsonProperty(value="horn_jump")
    private int hornJump;
    @JsonProperty(value="double_horn_jump")
    private int doubleHornJump;
    @JsonProperty(value="crystal_jump")
    private int crystalJump;

    public HistoryPlayerDTO(HistoryPlayer historyPlayer)
    {
        this.name = historyPlayer.getMemberPK().getName();
        this.score = historyPlayer.getScore();
        this.rank = historyPlayer.getRank();
        this.parseTime = historyPlayer.getMemberPK().getParseTime();
        this.season = historyPlayer.getSeason();
        this.minUnit = historyPlayer.getMinUnit();
        this.wave = historyPlayer.getWave();
        this.hornJump = historyPlayer.getHornJump();
        this.doubleHornJump = historyPlayer.getDhornJump();
        this.crystalJump = historyPlayer.getCrystalJump();
    }

    public static List<HistoryPlayerDTO> toDTO(List<HistoryPlayer> players) {
        List<HistoryPlayerDTO> list = new LinkedList<>();

        for (HistoryPlayer player : players) {
            list.add(new HistoryPlayerDTO(player));
        }
        return list;
    }

    public static HistoryPlayerDTO toDTO(HistoryPlayer player) {
        return new HistoryPlayerDTO(player);
    }

}
