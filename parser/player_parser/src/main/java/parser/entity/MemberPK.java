package parser.entity;


import javax.persistence.Column;
import javax.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class MemberPK implements Serializable {

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "parsetime", nullable = false)
    private LocalDateTime parseTime;

    public MemberPK() { }

    public MemberPK(String name, LocalDateTime parseTime) {
        this.name = name;
        this.parseTime = parseTime;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MemberPK)) {
            return false;
        }
        MemberPK that = (MemberPK) o;

        if (this.name != that.name) {
            return false;
        }
        if (this.parseTime != that.parseTime) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.parseTime);
    }
}

