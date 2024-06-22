package parser.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboard_hell")
public class LeaderboardHell extends LeaderboardBaseEntity {

    public LeaderboardHell() {
        super();
    }

    public LeaderboardHell(int rank, String name, int score) {
        super(rank, name, score);
    }

    public LeaderboardHell(int rank, String name, int score, LocalDateTime parseTime) {
        super(rank, name, score, parseTime);
    }

    public LeaderboardHell(LeaderboardBaseEntity leaderboard) {
        super(leaderboard.getRank(), leaderboard.getName(), leaderboard.getScore(), leaderboard.getParseTime());
    }

}