package parser.entity;

import java.time.LocalDateTime;

public class GuildMember {

    private String name;
    private int score;
    private LocalDateTime parseTime;

    public GuildMember() { }

    public GuildMember(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public GuildMember(String name, int score, LocalDateTime parseTime) {
        this.name = name;
        this.score = score;
        this.parseTime = parseTime;
    }


    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return this.score;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setParseTime(LocalDateTime parseTime) {
        this.parseTime = parseTime;
    }

    public LocalDateTime getParseTime() {
        return this.parseTime;
    }

}