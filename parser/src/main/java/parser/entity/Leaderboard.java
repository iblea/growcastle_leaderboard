package parser.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;

import kotlin.OverloadResolutionByLambdaReturnType;

import java.time.LocalDateTime;

@BatchSize(size = 100)
@MappedSuperclass
public class Leaderboard {

    @Id
    LeaderboardPK leaderboardPK;

    @Column(name = "rank")
    private int rank;

    @Column(name = "score")
    private int score;

    public Leaderboard() {
        this.leaderboardPK = new LeaderboardPK();
        this.rank = -1;
        this.score = -1;
    }

    public Leaderboard(int rank, String name, int score)
    {
        this.leaderboardPK = new LeaderboardPK();
        this.rank = rank;
        this.score = score;
        this.leaderboardPK.setName(name);
    }

    public Leaderboard(int rank, String name, int score, LocalDateTime parseTime)
    {
        this.leaderboardPK = new LeaderboardPK();
        this.rank = rank;
        this.score = score;
        this.leaderboardPK.setName(name);
        this.leaderboardPK.setParseTime(parseTime);
    }

    public int getRank() {
        return this.rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getName() {
        return this.leaderboardPK.getName();
    }

    public void setName(String name) {
        this.leaderboardPK.setName(name);
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getParseTime() {
        return this.leaderboardPK.getParseTime();
    }

    public void setParseTime(LocalDateTime parseTime) {
        this.leaderboardPK.setParseTime(parseTime);
    }

    public Leaderboard getLeaderboard() {
        return this;
    }
}

