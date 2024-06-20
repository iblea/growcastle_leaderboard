package parser.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboard_player")
public class LeaderboardPlayer extends Leaderboard {

    public LeaderboardPlayer() {
        super();
    }

    public LeaderboardPlayer(int rank, String name, int score) {
        super(rank, name, score);
    }

    public LeaderboardPlayer(int rank, String name, int score, LocalDateTime parseTime) {
        super(rank, name, score, parseTime);
    }

    public LeaderboardPlayer(Leaderboard leaderboard) {
        super(leaderboard.getRank(), leaderboard.getName(), leaderboard.getScore(), leaderboard.getParseTime());
    }
}