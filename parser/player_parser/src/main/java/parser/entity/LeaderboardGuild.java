package parser.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboard_guild")
public class LeaderboardGuild extends LeaderboardBaseEntity {

    public LeaderboardGuild() {
        super();
    }

    public LeaderboardGuild(int rank, String name, int score) {
        super(rank, name, score);
    }

    public LeaderboardGuild(int rank, String name, int score, LocalDateTime parseTime) {
        super(rank, name, score, parseTime);
    }

    public LeaderboardGuild(LeaderboardBaseEntity leaderboard) {
        super(leaderboard);
    }

}