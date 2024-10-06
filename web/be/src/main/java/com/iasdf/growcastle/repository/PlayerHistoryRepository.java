package com.iasdf.growcastle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iasdf.growcastle.domain.HistoryPlayer;
import com.iasdf.growcastle.domain.LeaderboardBaseEntity;
import com.iasdf.growcastle.domain.MemberPK;
import com.iasdf.growcastle.domain.QHistoryPlayer;
import com.iasdf.growcastle.domain.QHistoryPlayerSub;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
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
    QHistoryPlayer historyPlayer = new QHistoryPlayer("historyPlayer");
    QHistoryPlayer rankedData = new QHistoryPlayer("rankedData");
    QHistoryPlayerSub historyPlayerSub = new QHistoryPlayerSub("historyPlayerSub");

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
/*
WITH ranked_data AS (
    SELECT
        name,
        rank,
        score,
        parsetime,
        wave,
        hornjump,
        dhornjump,
        crystaljump,
        CASE
            WHEN EXTRACT(MINUTE FROM parsetime) > 0 THEN DATE_TRUNC('hour', parsetime) + INTERVAL '1 hour'
            ELSE DATE_TRUNC('hour', parsetime)
        END AS parsetime_1h
    FROM
        history_player
)
SELECT
    name,
    parsetime_1h,
	COALESCE(MAX(rank) FILTER (WHERE EXTRACT(MINUTE FROM parsetime) = 0), -1) AS rank,
    MAX(score) AS score,
    SUM(wave) AS total_wave,
    SUM(hornjump) AS total_hornjump,
    SUM(dhornjump) AS total_dhornjump,
    SUM(crystaljump) AS total_crystaljump
FROM
    ranked_data
WHERE
	LOWER(name) = LOWER('Ib')
GROUP BY
    name, parsetime_1h
ORDER BY
    name, parsetime_1h;
 */

        return queryFactory
            .select(
                // 생성자를 통한 매핑
                // Projections.constructor(
                //     HistoryPlayer.class,
                //     historyPlayerSub.memberPK.name,
                //     historyPlayerSub.parseTime1H.as("parseTime"),
                //     historyPlayerSub.rank.max().as("rank"),
                //     historyPlayerSub.score.max().as("score"),
                //     historyPlayerSub.wave.sum().as("wave"),
                //     historyPlayerSub.hornJump.sum().as("hornJump"),
                //     historyPlayerSub.dhornJump.sum().as("doubleHornJump"),
                //     historyPlayerSub.crystalJump.sum().as("crystalJump")
                // )

                // 디폴트 생성자가 필요함.
                // as를 통해 필드명을 객체의 이름에 맞게 매핑
                Projections.fields(
                    HistoryPlayer.class,
                    historyPlayerSub.memberPK.name,
                    historyPlayerSub.parseTime1H.as("parseTime"),
                    historyPlayerSub.score.max().as("score"),
                    Expressions.cases()
                        .when(historyPlayerSub.parseTime1H.minute().eq(0))
                        .then(historyPlayerSub.rank.max())
                        .otherwise(-1)
                        .as("rank"),
                    historyPlayerSub.wave.sum().as("wave"),
                    historyPlayerSub.hornJump.sum().as("hornJump"),
                    historyPlayerSub.dhornJump.sum().as("dhornJump"),
                    historyPlayerSub.crystalJump.sum().as("crystalJump")
                )
            ).from(historyPlayerSub)
            .where(
                historyPlayerSub.memberPK.name.toLowerCase().eq(name.toLowerCase())
            )
            .groupBy(historyPlayerSub.memberPK.name, historyPlayerSub.parseTime1H)
            .orderBy(historyPlayerSub.memberPK.name.asc(), historyPlayerSub.parseTime1H.asc())
            .fetch();
    }
}
