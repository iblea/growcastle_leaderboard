package parser.entity;

import javax.persistence.Column;
import javax.persistence.Id;

import javax.persistence.Entity;
import javax.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Table(name = "guild_member_wave")
public class GuildMemberWave {

    @Id
    private MemberPK memberPK;

    @Column(name = "guildname")
    private String guildName;

    @Column(name = "score")
    private int score;

    @Column(name = "season")
    private String season;

    @Column(name = "min_unit")
    private int minUnit;

    public GuildMemberWave() {
        this.memberPK = new MemberPK();
        this.guildName = "";
        this.score = 0;
        this.season = "";
        this.minUnit = 0;
    }

    public GuildMemberWave(String name, String guildName, int score,String season) {
        this.memberPK = new MemberPK();
        this.memberPK.setName(name);
        this.memberPK.setParseTime(LocalDateTime.now());
        this.guildName = guildName;
        this.score = score;
        this.season = season;
        this.minUnit = GuildMemberWave.getMinUnitAuto(this.memberPK.getParseTime());
    }

    public GuildMemberWave(String name, String guildName, int score, LocalDateTime parseTime, String season) {
        this.memberPK = new MemberPK();
        this.memberPK.setName(name);
        this.memberPK.setParseTime(parseTime);
        this.guildName = guildName;
        this.score = score;
        this.season = season;
        this.minUnit = GuildMemberWave.getMinUnitAuto(parseTime);
    }

    public GuildMemberWave(String name, String guildName, int score, LocalDateTime parseTime, String season, int minUnit) {
        this.memberPK = new MemberPK();
        this.memberPK.setName(name);
        this.memberPK.setParseTime(parseTime);
        this.guildName = guildName;
        this.score = score;
        this.season = season;
        this.minUnit = minUnit;
    }

    public void setName(String name) {
        this.memberPK.setName(name);
    }
    public String getName() {
        return this.memberPK.getName();
    }

    public void setParseTime(LocalDateTime parseTime) {
        this.memberPK.setParseTime(parseTime);
    }
    public LocalDateTime getParseTime() {
        return this.memberPK.getParseTime();
    }

    public void setGuildName(String guildName) {
        this.guildName = guildName;
    }
    public String getGuildName() {
        return this.guildName;
    }

    public void setScore(int score) {
        this.score = score;
    }
    public int getScore() {
        return this.score;
    }

    public void setSeason(String season) {
        this.season = season;
    }
    public String getSeason() {
        return this.season;
    }

    public void setMinUnit(int minUnit) {
        this.minUnit = minUnit;
    }
    public int getMinUnit() {
        return this.minUnit;
    }

    public static int getMinUnitAuto(LocalDateTime timeobj) {
        int minute = timeobj.getMinute();
        return (minute / 15) * 15;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GuildMemberWave)) {
            return false;
        }
        GuildMemberWave that = (GuildMemberWave) o;

        if (! this.getName().equals(that.getName())) {
            return false;
        }
        if (! this.getParseTime().equals(that.getParseTime())) {
            return false;
        }

        if (! this.guildName.equals(that.getGuildName())) {
            return false;
        }
        if (this.score != that.getScore()) {
            return false;
        }
        if (! this.season.equals(that.getSeason())) {
            return false;
        }
        if (this.minUnit != that.getMinUnit()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            GuildMemberWave.class,
            this.getName(),
            this.getParseTime(),
            this.guildName,
            this.score,
            this.season,
            this.minUnit
        );
    }
}