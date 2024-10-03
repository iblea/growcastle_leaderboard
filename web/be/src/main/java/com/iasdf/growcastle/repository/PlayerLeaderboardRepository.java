package com.iasdf.growcastle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iasdf.growcastle.domain.LeaderboardPlayer;
import com.iasdf.growcastle.domain.MemberPK;
import com.iasdf.growcastle.domain.QLeaderboardPlayer;
import com.querydsl.jpa.impl.JPAQueryFactory;


@Repository
public interface PlayerLeaderboardRepository extends JpaRepository<LeaderboardPlayer, MemberPK>, PlayerLeaderboardRepositoryCustom
{
    List<LeaderboardPlayer> findAll();
}

interface PlayerLeaderboardRepositoryCustom {

    List<LeaderboardPlayer> findsOffset(int limit, int offset);
}

// @RequiredArgsConstructor
class PlayerLeaderboardRepositoryCustomImpl implements PlayerLeaderboardRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private QLeaderboardPlayer qLeaderboardPlayer = new QLeaderboardPlayer("leaderboard_player");

    public PlayerLeaderboardRepositoryCustomImpl(final JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<LeaderboardPlayer> findsOffset(int limit, int offset)
    {
        // List<LeaderboardPlayer> result = null;
        return queryFactory.select(qLeaderboardPlayer)
            .from(qLeaderboardPlayer)
            .offset(offset)
            .limit(limit)
            .fetch();
    }
}
