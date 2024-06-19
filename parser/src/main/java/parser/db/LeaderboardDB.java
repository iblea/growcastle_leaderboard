package parser.db;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import parser.entity.Leaderboard;
import parser.entity.LeaderboardPK;

// 5분마다 800건
// 1일 = 24시간 = 1440분 = 288개
// 288 * 800 * 5

public class LeaderboardDB {

    Database db;

    public LeaderboardDB(Database db) {
        this.db = db;
    }

    public void insertLeaderboards(List<Leaderboard> data, Optional<String> type) {
        EntityManagerFactory emf = db.getEntityManagerFactory();
        if (emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (Leaderboard leaderboard : data) {
            em.persist(leaderboard);
        }
        try{
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    public void deleteLeaderboards(List<Leaderboard> data, Optional<String> type) {
        EntityManagerFactory emf = db.getEntityManagerFactory();
        if (emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (Leaderboard leaderboard : data) {
            em.remove(em.contains(leaderboard) ? leaderboard : em.merge(leaderboard));
            // em.remove(leaderboard);
        }
        try{
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }

    public Leaderboard findLeaderboardPK(String name, LocalDateTime parseTime) {
        EntityManagerFactory emf = db.getEntityManagerFactory();
        if (emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }

        LeaderboardPK pk = new LeaderboardPK(name, parseTime);
        Leaderboard leaderboard = null;
        EntityManager em = emf.createEntityManager();
        try {
            leaderboard = em.find(Leaderboard.class, pk);
        } finally {
            em.close();
        }
        return leaderboard;
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