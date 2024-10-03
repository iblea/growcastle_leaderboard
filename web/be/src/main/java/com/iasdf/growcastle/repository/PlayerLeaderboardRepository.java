package com.iasdf.growcastle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iasdf.growcastle.domain.LeaderboardPlayer;
import com.iasdf.growcastle.domain.MemberPK;

import lombok.NonNull;


@Repository
public interface PlayerLeaderboardRepository extends JpaRepository<LeaderboardPlayer, MemberPK>
{
    @NonNull List<LeaderboardPlayer> findAll();
}