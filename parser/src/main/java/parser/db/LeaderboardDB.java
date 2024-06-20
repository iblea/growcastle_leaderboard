package parser.db;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import parser.entity.Leaderboard;
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

    public void insertLeaderboards(List<Leaderboard> data, LeaderboardType type) {
        EntityManagerFactory emf = db.getEntityManagerFactory();
        if (emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        switch (type) {
            case PLAYER:
                for (Leaderboard leaderboard : data) {
                    em.persist(new LeaderboardPlayer(leaderboard));
                }
                break;
            case GUILD:
                for (Leaderboard leaderboard : data) {
                    em.persist(new LeaderboardGuild(leaderboard));
                }
                break;
            case HELL:
                for (Leaderboard leaderboard : data) {
                    em.persist(new LeaderboardHell(leaderboard));
                }
                break;
            default:
                break;
        }
        try {
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    public void deleteLeaderboards(List<Leaderboard> data, LeaderboardType type) {
        EntityManagerFactory emf = db.getEntityManagerFactory();
        if (emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (Leaderboard leaderboard : data) {
            Object entity;
            switch (type) {
                case PLAYER:
                    entity = new LeaderboardPlayer(leaderboard);
                    break;
                case GUILD:
                    entity = new LeaderboardGuild(leaderboard);
                    break;
                case HELL:
                    entity = new LeaderboardHell(leaderboard);
                    break;
                default:
                    entity = null;
                    break;
            }
            em.remove(em.contains(entity) ? entity: em.merge(entity));
        }
        try {
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    public Leaderboard findLeaderboardPK(String name, LocalDateTime parseTime, LeaderboardType type) {
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
                default:
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
            default:
                return null;
        }
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