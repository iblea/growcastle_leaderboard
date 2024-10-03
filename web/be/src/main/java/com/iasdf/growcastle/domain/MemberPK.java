package com.iasdf.growcastle.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.iasdf.growcastle.common.TimeUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter @Setter
public class MemberPK implements Serializable {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "parsetime", nullable = false)
    private LocalDateTime parseTime;

    public MemberPK() {
        this.name = "";
        this.parseTime = TimeUtil.getNow();
    }

    public MemberPK(String name) {
        this.name = name;
        this.parseTime = TimeUtil.getNow();
    }

    public MemberPK(String name, LocalDateTime parseTime) {
        this.name = name;
        this.parseTime = parseTime;
    }
}