package com.iasdf.growcastle.domain;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@BatchSize(size = 100)
@MappedSuperclass
@Getter @Setter
public class HistoryBaseEntity extends LeaderboardBaseEntity
{
    @Column(name = "season")
    private String season;

    @Column(name = "min_unit")
    private int minUnit;

    public HistoryBaseEntity() {
        super();
        this.season = "";
        this.minUnit = 0;
    }

    public HistoryBaseEntity(String name, LocalDateTime parseTime, int rank, int score)
    {
        super(name, parseTime, rank, score);
        this.season = "";
        this.minUnit = 0;
    }

    public HistoryBaseEntity(String name, LocalDateTime parseTime, int rank, int score, String season, int minUnit)
    {
        super(name, parseTime, rank, score);
        this.season = season;
        this.minUnit = minUnit;
    }

}

