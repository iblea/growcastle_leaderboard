package parser.entity;


import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.Objects;

@BatchSize(size = 100)
@MappedSuperclass
public class HistoryBaseEntity extends LeaderboardBaseEntity{

    @Column(name = "season")
    private String season;

    @Column(name = "min_unit")
    private int minUnit;

    public HistoryBaseEntity() {
        super();
        initHistoryValue();
    }

    public HistoryBaseEntity(int rank, String name, int score)
    {
        super(rank, name, score);
        initHistoryValue();
    }

    public HistoryBaseEntity(int rank, String name, int score, LocalDateTime parseTime)
    {

        super(rank, name, score, parseTime);
        initHistoryValue();
    }

    public HistoryBaseEntity(int rank, String name, int score, LocalDateTime parseTime, String season, int minUnit)
    {
        super(rank, name, score, parseTime);
        this.season = season;
        this.minUnit = minUnit;
    }

    public HistoryBaseEntity(LeaderboardBaseEntity entity)
    {
        super(entity);
        initHistoryValue();
    }

    public HistoryBaseEntity(LeaderboardBaseEntity entity, String season, int minUnit)
    {
        super(entity);
        this.season = season;
        this.minUnit = minUnit;
    }

    private void initHistoryValue() {
        this.season = "";
        this.minUnit = -1;
    }

    public void setSeason(String season)
    {
        this.season = season;
    }
    public String getSeason()
    {
        return this.season;
    }

    public void setMinUnit(int minUnit)
    {
        this.minUnit = minUnit;
    }
    public int getMinUnit()
    {
        return this.minUnit;
    }

    public HistoryBaseEntity getHistoryBaseEntity()
    {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HistoryBaseEntity)) {
            return false;
        }
        HistoryBaseEntity that = (HistoryBaseEntity) o;

        if (! this.getName().equals(that.getName())) {
            return false;
        }

        if (! this.getParseTime().equals(that.getParseTime())) {
            return false;
        }

        if (this.getRank() != that.getRank()) {
            return false;
        }

        if (this.getScore() != that.getScore()) {
            return false;
        }

        if (! this.season.equals(that.getSeason())) {
            return false;
        }

        if (this.minUnit != that.getMinUnit()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.getName(),
            this.getParseTime(),
            this.getRank(),
            this.getScore(),
            this.season,
            this.minUnit
        );
    }

}

