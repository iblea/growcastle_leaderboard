package parser.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "LEADERBOARD_PLAYER")
@SecondaryTables({
    @SecondaryTable(name = "LEADERBOARD_GUILD"),
    @SecondaryTable(name = "LEADERBOARD_HELL")
})
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

}

