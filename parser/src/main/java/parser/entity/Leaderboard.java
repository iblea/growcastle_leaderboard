package parser.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "Players")
@SecondaryTables({
    @SecondaryTable(name = "Guilds"),
    @SecondaryTable(name = "Hellmode")
})
public class Leaderboard {

    @Column(name = "rank")
    private int rank;

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "score")
    private int score;

    @Id
    @Column(name = "parseTime")
    private LocalDateTime parseTime;
    // private long parseTime;

    public Leaderboard() {
        this.rank = -1;
        this.name = "";
        this.score = -1;
        // this.parseTime = System.currentTimeMillis() / 1000;
    }

    public Leaderboard(int rank, String name, int score)
    {
        this.rank = rank;
        this.name = name;
        this.score = score;
    }

    public Leaderboard(int rank, String name, int score, LocalDateTime parseTime)
    {
        this.rank = rank;
        this.name = name;
        this.score = score;
    }

    public int getRank() {
        return this.rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getParseTime() {
        return this.parseTime;
    }

    public void setParseTime(LocalDateTime parseTime) {
        this.parseTime = parseTime;
    }
}