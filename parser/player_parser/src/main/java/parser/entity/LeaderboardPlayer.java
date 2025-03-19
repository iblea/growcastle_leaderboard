package parser.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "leaderboard_player")
public class LeaderboardPlayer extends LeaderboardBaseEntity {

    @Column(name = "wave")
    private int wave;

    @Column(name = "hornjump")
    private int hornJump;

    @Column(name = "dhornjump")
    private int dhornJump;  // double horn jump

    @Column(name = "crystaljump")
    private int crystalJump;


    public LeaderboardPlayer() {
        super();
    }

    public LeaderboardPlayer(int rank, String name, int score) {
        super(rank, name, score);
        this.wave = 0;
        this.hornJump = 0;
        this.dhornJump = 0;
        this.crystalJump = 0;
    }

    public LeaderboardPlayer(int rank, String name, int score, LocalDateTime parseTime) {
        super(rank, name, score, parseTime);
        this.wave = 0;
        this.hornJump = 0;
        this.dhornJump = 0;
        this.crystalJump = 0;
    }

    public LeaderboardPlayer(int rank, String name, int score, LocalDateTime parseTime, int wave, int hornJump, int dhornJump, int crystalJump) {
        super(rank, name, score, parseTime);
        this.wave = wave;
        this.hornJump = hornJump;
        this.dhornJump = dhornJump;
        this.crystalJump = crystalJump;
    }

    public LeaderboardPlayer(LeaderboardBaseEntity leaderboard) {
        super(leaderboard);
        this.wave = 0;
        this.hornJump = 0;
        this.dhornJump = 0;
        this.crystalJump = 0;
    }

    public LeaderboardPlayer(LeaderboardBaseEntity leaderboard, int wave, int hornJump, int dhornJump, int crystalJump) {
        super(leaderboard);
        this.wave = wave;
        this.hornJump = hornJump;
        this.dhornJump = dhornJump;
        this.crystalJump = crystalJump;
    }


    public void setWave(int wave) {
        this.wave = wave;
    }
    public void addWave(int wave) {
        this.wave += wave;
    }
    public int getWave() {
        return wave;
    }

    public void setHornJump(int hornJump) {
        this.hornJump = hornJump;
    }
    public void addHornJump(int hornJump) {
        this.hornJump += hornJump;
    }
    public int getHornJump() {
        return hornJump;
    }

    public void setDHornJump(int dhornJump) {
        this.dhornJump = dhornJump;
    }
    public void addDHornJump(int dhornJump) {
        this.dhornJump += dhornJump;
    }
    public int getDHornJump() {
        return dhornJump;
    }

    public void setCrystalJump(int crystalJump) {
        this.crystalJump = crystalJump;
    }
    public void addCrystalJump(int crystalJump) {
        this.crystalJump += crystalJump;
    }
    public int getCrystalJump() {
        return crystalJump;
    }

    public void setAgoData(LeaderboardPlayer agoData) {
        this.wave = agoData.getWave();
        this.hornJump = agoData.getHornJump();
        this.dhornJump = agoData.getDHornJump();
        this.crystalJump = agoData.getCrystalJump();
    }

    public void setWaveTracketZero() {
        this.wave = 0;
        this.hornJump = 0;
        this.dhornJump = 0;
        this.crystalJump = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LeaderboardPlayer)) {
            return false;
        }
        LeaderboardPlayer that = (LeaderboardPlayer) o;
        LeaderboardBaseEntity baseEntity = that.getLeaderboard();

        if (! super.equals(baseEntity)) {
            return false;
        }

        if (this.wave != that.getWave()) {
            return false;
        }
        if (this.hornJump != that.getHornJump()) {
            return false;
        }
        if (this.dhornJump != that.getDHornJump()) {
            return false;
        }
        if (this.crystalJump != that.getCrystalJump()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            LeaderboardPlayer.class,
            this.getRank(),
            this.getParseTime(),
            this.getRank(),
            this.getScore(),
            this.wave,
            this.hornJump,
            this.dhornJump,
            this.crystalJump
        );
    }

}