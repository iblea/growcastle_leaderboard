package parser.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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