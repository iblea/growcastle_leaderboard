package com.iasdf.growcastle.domain;

import java.lang.reflect.Member;
import java.time.LocalDateTime;

import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import com.querydsl.core.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Subselect(
    "SELECT " +
    "name, rank, score, parsetime, wave, hornjump, dhornjump, crystaljump," +
    "CASE " +
        "WHEN EXTRACT(MINUTE FROM parsetime) > 0 THEN DATE_TRUNC('hour', parsetime) + INTERVAL '1 hour'" +
        "ELSE DATE_TRUNC('hour', parsetime)" +
    "END AS parsetime_1h " +
    "FROM history_player"
)
@Immutable
@Synchronize("history_player")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter @Setter
public class HistoryPlayerSub {

    @EmbeddedId
    MemberPK memberPK;

    @Column(name = "rank")
    private int rank;
    @Column(name = "score")
    private int score;

    @Column(name = "parsetime_1h")
    private LocalDateTime parseTime1H;

    @Column(name = "wave")
    private int wave;
    @Column(name = "hornjump")
    private int hornJump;
    @Column(name = "dhornjump")
    private int dhornJump;
    @Column(name = "crystaljump")
    private int crystalJump;


    public HistoryPlayerSub(String name, LocalDateTime parseTime, int rank, int score, int wave, int hornJump, int dhornJump, int crystalJump, LocalDateTime parseTime1H) {
        this.memberPK = new MemberPK(name, parseTime);
        this.rank = rank;
        this.score = score;
        this.wave = wave;
        this.hornJump = hornJump;
        this.dhornJump = dhornJump;
        this.crystalJump = crystalJump;
        this.parseTime1H = parseTime1H;
    }

}
