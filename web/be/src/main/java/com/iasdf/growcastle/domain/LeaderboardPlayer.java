package com.iasdf.growcastle.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "leaderboard_player")
@Getter @Setter
public class LeaderboardPlayer extends LeaderboardBaseEntity
{
    @Column(name = "wave")
    private int wave;

    @Column(name = "hornjump")
    private int hornJump;

    @Column(name = "dhornjump")
    private int dhornJump;  // double horn jump

    @Column(name = "crystaljump")
    private int crystalJump;


}