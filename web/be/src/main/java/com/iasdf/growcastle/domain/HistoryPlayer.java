package com.iasdf.growcastle.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "history_player")
@Getter @Setter
public class HistoryPlayer extends HistoryBaseEntity {

    @Column(name = "wave")
    private int wave;

    @Column(name = "hornjump")
    private int hornJump;

    @Column(name = "dhornjump")
    private int dhornJump;  // double horn jump

    @Column(name = "crystaljump")
    private int crystalJump;

}