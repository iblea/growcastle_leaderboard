package com.iasdf.growcastle.repository;

import java.util.List;

import com.iasdf.growcastle.domain.LeaderboardPlayer;

public interface OPlayerLeaderboardRepositoryCustom
{

    List<LeaderboardPlayer> findsOffset(int limit, int offset);
}