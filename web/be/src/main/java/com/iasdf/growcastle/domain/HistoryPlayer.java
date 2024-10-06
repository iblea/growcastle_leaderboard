package com.iasdf.growcastle.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "history_player")
@Getter @Setter
public class HistoryPlayer extends HistoryBaseEntity
{

    @Column(name = "wave")
    private int wave;

    @Column(name = "hornjump")
    private int hornJump;

    @Column(name = "dhornjump")
    private int dhornJump;  // double horn jump

    @Column(name = "crystaljump")
    private int crystalJump;

    public HistoryPlayer() { }

    public HistoryPlayer(String name, LocalDateTime parseTime, int rank, int score,
        int wave, int hornJump, int dhornJump, int crystalJump) {

        super(name, parseTime, rank, score);
        this.wave = wave;
        this.hornJump = hornJump;
        this.dhornJump = dhornJump;
        this.crystalJump = crystalJump;
    }

    public HistoryPlayer(String name, LocalDateTime parseTime, int rank, int score,
        String season, int minUnit,
        int wave, int hornJump, int dhornJump, int crystalJump) {

        super(name, parseTime, rank, score, season, minUnit);
        this.wave = wave;
        this.hornJump = hornJump;
        this.dhornJump = dhornJump;
        this.crystalJump = crystalJump;
    }

}