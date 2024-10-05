package com.iasdf.growcastle.dto;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iasdf.growcastle.domain.LeaderboardPlayer;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LeaderboardPlayerDTO
{
    private String name;
    private int score;
    private int rank;
    @JsonProperty(value="parse_time")
    private LocalDateTime parseTime;

    private int wave;
    @JsonProperty(value="horn_jump")
    private int hornJump;
    @JsonProperty(value="double_horn_jump")
    private int doubleHornJump;
    @JsonProperty(value="crystal_jump")
    private int crystalJump;


    public LeaderboardPlayerDTO(LeaderboardPlayer player) {
        this.name = player.getMemberPK().getName();
        this.score = player.getScore();
        this.rank = player.getRank();
        this.parseTime = player.getMemberPK().getParseTime();
        this.wave = player.getWave();
        this.hornJump = player.getHornJump();
        this.doubleHornJump = player.getDhornJump();
        this.crystalJump = player.getCrystalJump();
    }

    public LeaderboardPlayerDTO(String name, int score, int rank) {
        this.name = name;
        this.score = score;
        this.rank = rank;
    }

    public static List<LeaderboardPlayerDTO> toDTO(List<LeaderboardPlayer> players) {
        List<LeaderboardPlayerDTO> list = new LinkedList<>();

        for (LeaderboardPlayer player : players) {
            list.add(new LeaderboardPlayerDTO(player));
        }
        return list;
    }

    public static LeaderboardPlayerDTO toDTO(LeaderboardPlayer player) {
        return new LeaderboardPlayerDTO(player);
    }
}