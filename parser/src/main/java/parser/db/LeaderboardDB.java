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
import parser.entity.LeaderboardGuild;
import parser.entity.LeaderboardPK;
import parser.entity.LeaderboardPlayer;
import parser.entity.LeaderboardHell;
import parser.parser.LeaderboardType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 5분마다 800건
// 1일 = 24시간 = 1440분 = 288개
// 288 * 800 * 5

public class LeaderboardDB {

    private static Logger logger = LogManager.getLogger(LeaderboardDB.class);

    Database db;

    public LeaderboardDB(Database db) {
        this.db = db;
    }

    public EntityManager makeTransaction() {
        EntityManagerFactory emf = db.getEntityManagerFactory();
        if (emf == null) {
            logger.error("EntityManagerFactory is null");
            throw new NullPointerException("EntityManagerFactory is null");
        }
        return emf.createEntityManager();
    }

    public void insertQuery(List<LeaderboardBaseEntity> data, LeaderboardType type, boolean isRealTime, EntityManager em) {
        if (isRealTime) {
            insertLeaderboardsByType(data, type, em);
        } else {
            insertLeaderboardsByTypeHistory(data, type, em);
        }
    }

    public boolean insertLeaderboards(List<LeaderboardBaseEntity> data, LeaderboardType type, boolean isRealTime) {
        EntityManager em = makeTransaction();
        EntityTransaction transaction = em.getTransaction();
        boolean result = true;
        try {
            transaction.begin();
            insertQuery(data, type, isRealTime, em);
            transaction.commit();
        } catch (Exception e) {
            logger.error("insertLeaderboards error");
            logger.error(e.getMessage());
            if (transaction.isActive()) {
                transaction.rollback();
            }
            result = false;
        } finally {
            em.close();
        }
        logger.debug("[{}][{}][{}] leaderboards inserted", type.getTypename(), data.size(), isRealTime);
        return result;
    }

    private void insertLeaderboardsByType(List<LeaderboardBaseEntity> data, LeaderboardType type, EntityManager em) {
        switch (type) {
            case PLAYER:
                for (LeaderboardBaseEntity leaderboard : data) {
                    em.persist(new LeaderboardPlayer(leaderboard));
                }
                break;
            case GUILD:
                for (LeaderboardBaseEntity leaderboard : data) {
                    em.persist(new LeaderboardGuild(leaderboard));
                }
                break;
            case HELL:
                for (LeaderboardBaseEntity leaderboard : data) {
                    em.persist(new LeaderboardHell(leaderboard));
                }
                break;
        }
    }

    private void insertLeaderboardsByTypeHistory(List<LeaderboardBaseEntity> data, LeaderboardType type, EntityManager em) {
        switch (type) {
            case PLAYER:
                for (LeaderboardBaseEntity leaderboard : data) {
                    em.persist(new HistoryPlayer(leaderboard));
                }
                break;
            case GUILD:
                for (LeaderboardBaseEntity leaderboard : data) {
                    em.persist(new HistoryGuild(leaderboard));
                }
                break;
            case HELL:
                for (LeaderboardBaseEntity leaderboard : data) {
                    em.persist(new HistoryHell(leaderboard));
                }
                break;
        }
    }

    public void deleteHistoryLeaderboardsUntilDate(LocalDateTime date) {
        for (LeaderboardType type : LeaderboardType.values()) {
            deleteLeaderboardsUntilDateWithType(date, type.getHistoryTableName());
        }
    }

    public void deleteLeaderboardsUntilDateWithType(LocalDateTime date, String tableName) {
        EntityManager em = makeTransaction();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            String sql = "DELETE FROM `" + tableName + "` WHERE parseTime < :date";
            Query query = em.createNativeQuery(sql);
            query.setParameter("date", date);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("deleteLeaderboardsUntilDateWithType error");
            logger.error(e.getMessage());
            if (transaction.isActive()) {
                transaction.rollback();
            }
        } finally {
            em.close();
        }
        logger.debug("date : [{}], tableName : [{}]", date, tableName);
        logger.debug("deleteLeaderboardsUntilDateWithType success");
    }

    public void deleteAllQuery(String tableName, EntityManager em) {
        String sql = "DELETE FROM `" + tableName + "`";
        Query query = em.createNativeQuery(sql);
        query.executeUpdate();
    }

    public void deleteLeaderboards(List<LeaderboardBaseEntity> data, LeaderboardType type, boolean isRealTime) {
        EntityManager em = makeTransaction();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            if (isRealTime) {
                deleteLeaderboardsByType(data, type, em);
            } else {
                deleteLeaderboardsByTypeHistory(data, type, em);
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("deleteLeaderboards error");
            logger.error(e.getMessage());
            if (transaction.isActive()) {
                transaction.rollback();
            }
        } finally {
            em.close();
        }
        logger.debug("[{}][{}] leaderboards deleted", type.getTypename(), data.size());
    }

    private void deleteLeaderboardsByType(List<LeaderboardBaseEntity> data, LeaderboardType type, EntityManager em) {
        switch (type) {
            case PLAYER:
                for (LeaderboardBaseEntity leaderboard : data) {
                    LeaderboardPlayer player = new LeaderboardPlayer(leaderboard);
                    em.remove(em.contains(player) ? player : em.merge(player));
                }
                break;
            case GUILD:
                for (LeaderboardBaseEntity leaderboard : data) {
                    LeaderboardGuild guild = new LeaderboardGuild(leaderboard);
                    em.remove(em.contains(guild) ? guild : em.merge(guild));
                }
                break;
            case HELL:
                for (LeaderboardBaseEntity leaderboard : data) {
                    LeaderboardHell hell = new LeaderboardHell(leaderboard);
                    em.remove(em.contains(hell) ? hell : em.merge(hell));
                }
                break;
        }
    }

    private void deleteLeaderboardsByTypeHistory(List<LeaderboardBaseEntity> data, LeaderboardType type, EntityManager em) {
        switch (type) {
            case PLAYER:
                for (LeaderboardBaseEntity leaderboard : data) {
                    HistoryPlayer player = new HistoryPlayer(leaderboard);
                    em.remove(em.contains(player) ? player : em.merge(player));
                }
                break;
            case GUILD:
                for (LeaderboardBaseEntity leaderboard : data) {
                    HistoryGuild guild = new HistoryGuild(leaderboard);
                    em.remove(em.contains(guild) ? guild : em.merge(guild));
                }
                break;
            case HELL:
                for (LeaderboardBaseEntity leaderboard : data) {
                    HistoryHell hell = new HistoryHell(leaderboard);
                    em.remove(em.contains(hell) ? hell : em.merge(hell));
                }
                break;
        }
    }


    public LeaderboardBaseEntity findLeaderboardPK(String name, LocalDateTime parseTime, LeaderboardType type) {
        LeaderboardPK pk = new LeaderboardPK(name, parseTime);
        Object leaderboard = null;
        EntityManager em = makeTransaction();
        try {
            switch (type) {
                case PLAYER:
                    leaderboard = em.find(LeaderboardPlayer.class, pk);
                    break;
                case GUILD:
                    leaderboard = em.find(LeaderboardGuild.class, pk);
                    break;
                case HELL:
                    leaderboard = em.find(LeaderboardHell.class, pk);
                    break;
            }
        } catch (Exception e) {
            logger.error("findLeaderboardPK error");
            logger.error(e.getMessage());
        } finally {
            em.close();
        }
        if (leaderboard == null) {
            return null;
        }

        switch (type) {
            case PLAYER:
                return ((LeaderboardPlayer) leaderboard).getLeaderboard();
            case GUILD:
                return ((LeaderboardGuild) leaderboard).getLeaderboard();
            case HELL:
                return ((LeaderboardHell) leaderboard).getLeaderboard();
        }
        return null;
    }

    public boolean updateLeaderboards(List<LeaderboardBaseEntity> data, LeaderboardType type) {
        EntityManager em = makeTransaction();
        boolean result = true;
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            deleteAllQuery(type.getRealTimeTableName(), em);
            insertQuery(data, type, true, em);
            transaction.commit();
        } catch (Exception e) {
            logger.error("insertLeaderboards error");
            logger.error(e.getMessage());
            if (transaction.isActive()) {
                transaction.rollback();
            }
            result = false;
        } finally {
            em.close();
        }
        logger.debug("[{}][{}] leaderboards inserted", type.getTypename(), data.size());
        return result;
    }

    public LocalDateTime getParseTime() {
        LocalDateTime now = LocalDateTime.now();
        int minutes = now.getMinute();
        int roundedMinutes = (minutes / 5) * 5;
        return now.withMinute(roundedMinutes)
            .withSecond(0)
            .withNano(0)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toLocalDateTime();
    }

    /**
     * 파싱한 현재 시간을 5분단위로 내려 Unix Time을 리턴한다. (Asia/Seoul - KST 기준)
     * @return long
     */
    public long getParseTimeUnix() {
        return getParseTime().atZone(ZoneId.of("Asia/Seoul")).toEpochSecond();
    }
}