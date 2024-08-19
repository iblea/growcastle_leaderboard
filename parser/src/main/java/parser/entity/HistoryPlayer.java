package parser.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import java.util.Objects;
import java.time.LocalDateTime;

@Entity
@Table(name = "history_player")
public class HistoryPlayer extends HistoryBaseEntity {

    @Column(name = "wave")
    private int wave;

    @Column(name = "hornjump")
    private int hornJump;

    @Column(name = "dhornjump")
    private int dhornJump;  // double horn jump

    @Column(name = "crystaljump")
    private int crystalJump;


    public HistoryPlayer() {
        super();
        this.wave = 0;
        this.hornJump = 0;
        this.dhornJump = 0;
        this.crystalJump = 0;
    }

    public HistoryPlayer(LeaderboardBaseEntity entity) {
        super(entity);
        this.wave = 0;
        this.hornJump = 0;
        this.dhornJump = 0;
        this.crystalJump = 0;
    }

    public HistoryPlayer(LeaderboardBaseEntity entity, String season, int minUnit) {
        super(entity, season, minUnit);
        this.wave = 0;
        this.hornJump = 0;
        this.dhornJump = 0;
        this.crystalJump = 0;
    }

    public HistoryPlayer(LeaderboardBaseEntity entity, String season, int minUnit, int wave, int hornJump, int dhornJump, int crystalJump) {
        super(entity, season, minUnit);
        this.wave = wave;
        this.hornJump = hornJump;
        this.dhornJump = dhornJump;
        this.crystalJump = crystalJump;
    }


    public HistoryPlayer(HistoryBaseEntity entity) {
        super(entity);
        this.wave = 0;
        this.hornJump = 0;
        this.dhornJump = 0;
        this.crystalJump = 0;
    }

    public HistoryPlayer(HistoryBaseEntity entity, int wave, int hornJump, int dhornJump, int crystalJump) {
        super(entity);
        this.wave = wave;
        this.hornJump = hornJump;
        this.dhornJump = dhornJump;
        this.crystalJump = crystalJump;
    }

    public HistoryPlayer(LeaderboardPlayer entity, String season, int minUnit, LocalDateTime time) {
        super(entity.getRank(), entity.getName(), entity.getScore(), entity.getParseTime(), season, minUnit);
        this.setParseTime(time);
        this.wave = entity.getWave();
        this.hornJump = entity.getHornJump();
        this.dhornJump = entity.getDHornJump();
        this.crystalJump = entity.getCrystalJump();
    }


    public void setWave(int wave) {
        this.wave = wave;
    }
    public int getWave() {
        return wave;
    }

    public void setHornJump(int hornJump) {
        this.hornJump = hornJump;
    }
    public int getHornJump() {
        return hornJump;
    }

    public void setDhornJump(int dhornJump) {
        this.dhornJump = dhornJump;
    }
    public int getDhornJump() {
        return dhornJump;
    }

    public void setCrystalJump(int crystalJump) {
        this.crystalJump = crystalJump;
    }
    public int getCrystalJump() {
        return crystalJump;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof HistoryPlayer)) {
            return false;
        }

        HistoryPlayer that = (HistoryPlayer) o;
        HistoryBaseEntity baseEntity = that.getHistoryBaseEntity();

        if (! super.equals(baseEntity)) {
            return false;
        }

        if (this.wave != that.getWave()) {
            return false;
        }
        if (this.hornJump != that.getHornJump()) {
            return false;
        }
        if (this.dhornJump != that.getDhornJump()) {
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
            HistoryPlayer.class,
            this.getName(),
            this.getParseTime(),
            this.getRank(),
            this.getScore(),
            this.getSeason(),
            this.getMinUnit(),
            this.wave,
            this.hornJump,
            this.dhornJump,
            this.crystalJump
        );
    }

}