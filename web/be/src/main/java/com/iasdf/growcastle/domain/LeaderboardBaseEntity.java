package com.iasdf.growcastle.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@BatchSize(size = 100)
@MappedSuperclass
@Getter @Setter
public class LeaderboardBaseEntity
{
    @Id
    private MemberPK memberPK;

    @Column(name = "rank")
    private int rank;

    @Column(name = "score")
    private int score;


    public void setMemberPK(MemberPK memberPK) {
        this.memberPK = memberPK;
    }

    public LeaderboardBaseEntity() {
        this.memberPK = new MemberPK();
        this.rank = 0;
        this.score = 0;
    }

    public LeaderboardBaseEntity(String name, LocalDateTime parseTime, int rank, int score)
    {
        this.memberPK = new MemberPK(name, parseTime);
        this.rank = rank;
        this.score = score;
    }

}