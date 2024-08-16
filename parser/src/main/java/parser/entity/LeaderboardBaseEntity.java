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
    private MemberPK memberPK;

    @Column(name = "rank")
    private int rank;

    @Column(name = "score")
    private int score;

    public LeaderboardBaseEntity() {
        this.memberPK = new MemberPK();
        this.rank = -1;
        this.score = -1;
    }

    public LeaderboardBaseEntity(int rank, String name, int score)
    {
        this.memberPK = new MemberPK();
        this.rank = rank;
        this.score = score;
        this.memberPK.setName(name);
    }

    public LeaderboardBaseEntity(int rank, String name, int score, LocalDateTime parseTime)
    {
        this.memberPK = new MemberPK();
        this.rank = rank;
        this.score = score;
        this.memberPK.setName(name);
        this.memberPK.setParseTime(parseTime);
    }

    public LeaderboardBaseEntity(LeaderboardBaseEntity entity)
    {
        this.memberPK = new MemberPK(entity.getName(), entity.getParseTime());
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
        return this.memberPK.getName();
    }

    public void setName(String name) {
        this.memberPK.setName(name);
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getParseTime() {
        return this.memberPK.getParseTime();
    }

    public void setParseTime(LocalDateTime parseTime) {
        this.memberPK.setParseTime(parseTime);
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

        if (! this.memberPK.equals(that.memberPK)) {
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
            this.memberPK.getName(),
            this.memberPK.getParseTime(),
            this.rank,
            this.score
        );
    }

}

