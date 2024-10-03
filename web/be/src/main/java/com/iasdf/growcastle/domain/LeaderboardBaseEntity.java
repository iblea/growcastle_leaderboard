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
class LeaderboardBaseEntity {
    @Id
    private MemberPK memberPK;

    @Column(name = "rank")
    private int rank;

    @Column(name = "score")
    private int score;


    public void setMemberPK(MemberPK memberPK) {
        this.memberPK = memberPK;
    }

}