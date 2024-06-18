package parser.entity;


import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;

@Embeddable
public class LeaderboardPK implements Serializable {

    @Column(name = "name", nullable = false)
    private String name;
    private LocalDateTime parseTime;

    public LeaderboardPK() { }

    public LeaderboardPK(String name, LocalDateTime parseTime) {
        this.name = name;
        this.parseTime = parseTime;
    }

    @Column(name = "parsetime")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getParseTime() {
        return this.parseTime;
    }

    public void setParseTime(LocalDateTime parseTime) {
        this.parseTime = parseTime;
    }

}

