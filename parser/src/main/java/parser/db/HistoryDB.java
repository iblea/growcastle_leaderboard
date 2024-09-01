package parser.db;

import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import parser.entity.HistoryGuild;
import parser.entity.HistoryHell;
import parser.entity.HistoryPlayer;
import parser.entity.LeaderboardBaseEntity;
import parser.entity.LeaderboardPlayer;
import parser.entity.MemberPK;
import parser.parser.LeaderboardType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

        int minUnit = getMinUnit(time);
        if (data.isEmpty()) {
            logger.warn("insert data is empty");
            return false;
        }

        try {
            transaction.begin();
            for (LeaderboardPlayer leaderboard : data) {
                this.em.persist(new HistoryPlayer(leaderboard, seasonName, minUnit, time));
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("insertHistory error");
            logger.error(e.getMessage());
            UtilDB.transactionRollback(transaction);
            result = false;
        }
        logger.debug("player [{}] historyPlayerTracking inserted", data.size());
        return result;
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
                for (LeaderboardBaseEntity leaderboard : data) {
                    this.em.persist(new HistoryGuild(leaderboard, seasonName, minUnit, time));
                }
                break;
            case HELL:
                for (LeaderboardBaseEntity leaderboard : data) {
                    this.em.persist(new HistoryHell(leaderboard, seasonName, minUnit, time));
                }
                break;
        }
    }

    public void deleteHistoryUntilDate(LocalDateTime date) {
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