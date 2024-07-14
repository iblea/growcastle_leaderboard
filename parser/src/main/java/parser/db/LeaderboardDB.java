package parser.db;

import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import parser.entity.LeaderboardBaseEntity;
import parser.entity.LeaderboardGuild;
import parser.entity.LeaderboardPK;
import parser.entity.LeaderboardPlayer;
import parser.entity.LeaderboardHell;
import parser.parser.LeaderboardType;

// 5분마다 800건
// 1일 = 24시간 = 1440분 = 288개
// 288 * 800 * 5

public class LeaderboardDB {

    Database db;

    public LeaderboardDB(Database db) {
        this.db = db;
    }

    public void insertLeaderboards(List<LeaderboardBaseEntity> data, LeaderboardType type) {
        EntityManagerFactory emf = db.getEntityManagerFactory();
        if (emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        insertLeaderboardsByType(data, type, em);
        try {
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
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

    public void deleteLeaderboardsUntilDate(LocalDateTime date) {
        deleteLeaderboardsUntilDateWithType(date, "LEADERBOARD_PLAYER");
        deleteLeaderboardsUntilDateWithType(date, "LEADERBOARD_GUILD");
        deleteLeaderboardsUntilDateWithType(date, "LEADERBOARD_HELL");
    }

    public void deleteLeaderboardsUntilDateWithType(LocalDateTime date, String tableName) {
        EntityManagerFactory emf = db.getEntityManagerFactory();

        if (emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            String sql = "DELETE FROM `" + tableName + "` WHERE parseTime < :date";
            Query query = em.createNativeQuery(sql);
            query.setParameter("date", date);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (transaction.isActive()) {
                transaction.rollback();
            }
        } finally {
            em.close();
        }

    }

    public void deleteLeaderboards(List<LeaderboardBaseEntity> data, LeaderboardType type) {
        EntityManagerFactory emf = db.getEntityManagerFactory();
        if (emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        deleteLeaderboardsByType(data, type, em);
        try {
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
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

    public LeaderboardBaseEntity findLeaderboardPK(String name, LocalDateTime parseTime, LeaderboardType type) {
        EntityManagerFactory emf = db.getEntityManagerFactory();
        if (emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }

        LeaderboardPK pk = new LeaderboardPK(name, parseTime);
        Object leaderboard = null;
        EntityManager em = emf.createEntityManager();
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