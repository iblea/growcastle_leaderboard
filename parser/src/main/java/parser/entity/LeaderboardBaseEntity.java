package parser.entity;


import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.Objects;

@BatchSize(size = 100)
@MappedSuperclass
public class LeaderboardBaseEntity {

    @Id
    private LeaderboardPK leaderboardPK;

    @Column(name = "rank")
    private int rank;

    @Column(name = "score")
    private int score;

    public LeaderboardBaseEntity() {
        this.leaderboardPK = new LeaderboardPK();
        this.rank = -1;
        this.score = -1;
    }

    public LeaderboardBaseEntity(int rank, String name, int score)
    {
        this.leaderboardPK = new LeaderboardPK();
        this.rank = rank;
        this.score = score;
        this.leaderboardPK.setName(name);
    }

    public LeaderboardBaseEntity(int rank, String name, int score, LocalDateTime parseTime)
    {
        this.leaderboardPK = new LeaderboardPK();
        this.rank = rank;
        this.score = score;
        this.leaderboardPK.setName(name);
        this.leaderboardPK.setParseTime(parseTime);
    }

    public LeaderboardBaseEntity(LeaderboardBaseEntity entity)
    {
        this.leaderboardPK = new LeaderboardPK(entity.getName(), entity.getParseTime());
        this.rank = entity.getRank();
        this.score = entity.getScore();
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

    public LeaderboardBaseEntity getLeaderboard() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LeaderboardBaseEntity)) {
            return false;
        }
        LeaderboardBaseEntity that = (LeaderboardBaseEntity) o;

        if (! this.leaderboardPK.equals(that.leaderboardPK)) {
            return false;
        }
        if (this.rank != that.rank) {
            return false;
        }
        if (this.score != that.score) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.leaderboardPK.getName(),
            this.leaderboardPK.getParseTime(),
            this.rank,
            this.score
        );
    }

}

