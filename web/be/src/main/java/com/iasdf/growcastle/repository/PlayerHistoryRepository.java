package com.iasdf.growcastle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iasdf.growcastle.domain.HistoryPlayer;
import com.iasdf.growcastle.domain.LeaderboardBaseEntity;
import com.iasdf.growcastle.domain.LeaderboardPlayer;
import com.iasdf.growcastle.domain.MemberPK;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public interface PlayerHistoryRepository extends JpaRepository<LeaderboardBaseEntity, MemberPK>, PlayerHistoryRepositoryCustom
{
}

interface PlayerHistoryRepositoryCustom
{
    HistoryPlayer findPlayerInfo(String name, int unit);
    List<HistoryPlayer> findPlayerHistory(String name, int unit);
}

class PlayerHistoryRepositoryCustomImpl implements PlayerHistoryRepositoryCustom
{
    private final JPAQueryFactory queryFactory;

    public PlayerHistoryRepositoryCustomImpl(final JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public HistoryPlayer findPlayerInfo(String name, int unit)
    {
        System.out.println("findPlayerInfo");
        return null;
    }

    @Override
    public List<HistoryPlayer> findPlayerHistory(String name, int unit)
    {
        return null;
    }
}
