package com.iasdf.growcastle.repository;

import java.util.LinkedList;
import java.util.List;

import com.iasdf.growcastle.domain.LeaderboardPlayer;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OPlayerLeaderboardRepositoryCustomImpl implements OPlayerLeaderboardRepositoryCustom
{

    private final EntityManager em;

    // private final JPAQueryFactory queryFactory;
    // public MemberRepositoryCustomImpl(EntityManager em) {
    //     this.queryFactory = new JPAQueryFactory(em);
    // }

    @Override
    public List<LeaderboardPlayer> findsOffset(int limit, int offset) {
        if (limit < 0) {
            return null;
        }
        return new LinkedList<>();
    }
}
