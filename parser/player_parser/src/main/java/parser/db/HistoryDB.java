package parser.db;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import parser.entity.HistoryGuild;
import parser.entity.HistoryHell;
import parser.entity.HistoryPlayer;
import parser.entity.LeaderboardBaseEntity;
import parser.entity.LeaderboardPlayer;
import parser.entity.MemberPK;
import parser.parser.LeaderboardType;

// 5분마다 800건
// 1일 = 24시간 = 1440분 = 288개
// 288 * 800 * 5

public class HistoryDB {

    private static Logger logger = LogManager.getLogger(HistoryDB.class);

    private Database db;
    private EntityManager em;

    public HistoryDB(Database db) {
        this.db = db;
        this.em = UtilDB.setEntityManager(this.db);
    }

    public void clearEntityManager() {
        if ( this.em != null && this.em.isOpen() ) {
            this.em.clear();
        }
    }

    public void closeEntityManager() {
        if ( UtilDB.closeEntityManager(this.em) ) {
            this.em = null;
        }
    }


    public boolean insertHistory(List<LeaderboardBaseEntity> data, LeaderboardType type, String seasonName, LocalDateTime time) {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return false;
        }
        EntityTransaction transaction = this.em.getTransaction();
        boolean result = true;

        if (data.isEmpty()) {
            logger.warn("insert data is empty");
            return false;
        }

        try {
            transaction.begin();
            insertHistoryByType(data, type, seasonName, time);
            transaction.commit();
        } catch (Exception e) {
            logger.error("insertHistory error");
            logger.error(e.getMessage());
            UtilDB.transactionRollback(transaction);
            result = false;
        }
        logger.debug("[{}][{}] history inserted", type.getTypename(), data.size());
        return result;
    }

    public boolean insertHistoryPlayerTracking(List<LeaderboardPlayer> data, String seasonName, LocalDateTime time) {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return false;
        }
        EntityTransaction transaction = this.em.getTransaction();
        boolean result = true;

        if (data == null) {
            logger.error("insert data is null");
            return false;
        }
        if (data.isEmpty()) {
            logger.warn("insert data is empty");
            return false;
        }

        // LocalDateTime hourTime = time.withMinute(0).withSecond(0).withNano(0);
        // int minUnit = getMinUnit(hourTime);


        // 만약 insert 시키고 있는 현재 시간이 15:00:30 이라면, 14:55:00 ~ 15:00:00 까지의 웨이브 변경을 누적시키는 것이기 때문에 시간 조정 필요.
        LocalDateTime standardTime = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusMinutes(5);
        int minUnit = (standardTime.getMinute() / 5) * 5 + 5;
        LocalDateTime hourTime = standardTime.withMinute(0).withSecond(0).withNano(0);
        if (data.isEmpty()) {
            logger.warn("insert data is empty");
            return false;
        }
        // System.out.println("CONSOLE | standardTime: " + standardTime);
        // System.out.println("CONSOLE | minUnit: " + minUnit);
        // System.out.println("CONSOLE | hourTime: " + hourTime);

        try {
            transaction.begin();
            String jpql = "UPDATE " + HistoryPlayer.class.getSimpleName() + " h SET" +
                    " h.rank = :rank," +
                    " h.score = :score," +
                    " h.minUnit = :minUnit," +
                    " h.wave = h.wave + :wave," +
                    " h.hornJump = h.hornJump + :hornJump," +
                    " h.dhornJump = h.dhornJump + :dhornJump," +
                    " h.crystalJump = h.crystalJump + :crystalJump" +
                    " WHERE name = :name and parseTime = :parseTime";

            int totalInsertedCount = 0;
            for (LeaderboardPlayer leaderboard : data) {
                int updatedCount = this.em.createQuery(jpql)
                    .setParameter("rank", leaderboard.getRank())
                    .setParameter("score", leaderboard.getScore())
                    .setParameter("minUnit", minUnit)
                    .setParameter("wave", leaderboard.getWave())
                    .setParameter("hornJump", leaderboard.getHornJump())
                    .setParameter("dhornJump", leaderboard.getDHornJump())
                    .setParameter("crystalJump", leaderboard.getCrystalJump())
                    .setParameter("name", leaderboard.getName())
                    .setParameter("parseTime", hourTime)
                    .executeUpdate();
                if (updatedCount == 0) {
                    this.em.persist(new HistoryPlayer(leaderboard, seasonName, minUnit, hourTime));
                    totalInsertedCount++;
                }
                // this.em.merge(new HistoryPlayer(leaderboard, seasonName, minUnit, hourTime));
            }
            transaction.commit();
            logger.debug("insertedCount (player) : [{}]", totalInsertedCount);
        } catch (Exception e) {
            logger.error("insertHistory error");
            logger.error(e.getMessage());
            UtilDB.transactionRollback(transaction);
            result = false;
        }
        logger.debug("player [{}] historyPlayerTracking inserted", data.size());
        return result;
    }

    private void upsertHistory(List<LeaderboardBaseEntity> data, LeaderboardType type, String seasonName, LocalDateTime time) {
        String jpql;
        if (type == LeaderboardType.GUILD) {
            jpql = "UPDATE " + HistoryGuild.class.getSimpleName() + " h SET";
        } else if (type == LeaderboardType.HELL) {
            jpql = "UPDATE " + HistoryHell.class.getSimpleName() + " h SET";
        } else {
            logger.error("invalid type");
            return;
        }

        // String jpql = "UPDATE " + type.getHistoryTableName() + " h SET" +
        jpql +=
            " h.rank = :rank," +
            " h.score = :score" +
            " WHERE name = :name and parseTime = :parseTime";
        LocalDateTime hourTime = time.withMinute(0).withSecond(0).withNano(0);
        int totalInsertedCount = 0;
        for (LeaderboardBaseEntity leaderboard : data) {
            int updatedCount = this.em.createQuery(jpql)
                .setParameter("rank", leaderboard.getRank())
                .setParameter("score", leaderboard.getScore())
                .setParameter("name", leaderboard.getName())
                .setParameter("parseTime", hourTime)
                .executeUpdate();
            if (updatedCount == 0) {
                totalInsertedCount++;
                if (type == LeaderboardType.GUILD) {
                    this.em.persist(new HistoryGuild(leaderboard, seasonName, 0, hourTime));
                } else if (type == LeaderboardType.HELL) {
                    this.em.persist(new HistoryHell(leaderboard, seasonName, 0, hourTime));
                }
            }
        }
        logger.debug("insertedCount (other) : [{}]", totalInsertedCount);
    }


    private void insertHistoryByType(List<LeaderboardBaseEntity> data, LeaderboardType type, String seasonName, LocalDateTime time) {
        int minUnit = getMinUnit(data.get(0).getParseTime());
        switch (type) {
            case PLAYER:
                HistoryPlayer history;
                for (LeaderboardBaseEntity leaderboard : data) {
                    history = new HistoryPlayer(leaderboard, seasonName, minUnit);
                    history.setParseTime(time);
                    this.em.persist(history);
                }
                break;
            case GUILD:
                this.upsertHistory(data, type, seasonName, time);
                break;
            case HELL:
                this.upsertHistory(data, type, seasonName, time);
                break;
        }
    }

    public void deleteHistoryUntilDate(LocalDateTime date) {
        date = date.minusMinutes(10);
        for (LeaderboardType type : LeaderboardType.values()) {
            deleteHistoryUntilDateWithType(date, type.getHistoryTableName());
        }
    }

    public void deleteHistoryUntilDateWithType(LocalDateTime date, String tableName) {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return ;
        }
        EntityTransaction transaction = this.em.getTransaction();
        try {
            transaction.begin();
            String sql = "DELETE FROM " + tableName + " WHERE parseTime < :date";
            Query query = this.em.createNativeQuery(sql);
            query.setParameter("date", date);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("deleteHistoryUntilDateWithType error");
            logger.error(e.getMessage());
            UtilDB.transactionRollback(transaction);
        }
        logger.debug("date : [{}], tableName : [{}]", date, tableName);
        logger.debug("deleteHistoryUntilDateWithType success");
    }

    public void deleteAllQuery(String tableName) {
        String sql = "DELETE FROM " + tableName + "";
        Query query = this.em.createNativeQuery(sql);
        query.executeUpdate();
    }

    public LeaderboardBaseEntity findHistoryPK(String name, LocalDateTime parseTime, LeaderboardType type) {
        MemberPK pk = new MemberPK(name, parseTime);
        Object leaderboard = null;
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return null;
        }
        try {
            switch (type) {
                case PLAYER:
                    leaderboard = this.em.find(HistoryPlayer.class, pk);
                    break;
                case GUILD:
                    leaderboard = this.em.find(HistoryGuild.class, pk);
                    break;
                case HELL:
                    leaderboard = this.em.find(HistoryHell.class, pk);
                    break;
            }
        } catch (Exception e) {
            logger.error("findLeaderboardPK error");
            logger.error(e.getMessage());
        }
        if (leaderboard == null) {
            return null;
        }

        switch (type) {
            case PLAYER:
                return ((HistoryPlayer) leaderboard).getLeaderboard();
            case GUILD:
                return ((HistoryGuild) leaderboard).getLeaderboard();
            case HELL:
                return ((HistoryHell) leaderboard).getLeaderboard();
        }
        return null;
    }

    public int getMinUnit(LocalDateTime timeobj) {
        int minute = timeobj.getMinute();
        return (minute / 15) * 15;
    }


}