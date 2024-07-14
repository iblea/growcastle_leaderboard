package parser.db;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import parser.entity.GuildMember;

public class GuildMemberDB {

    Database db;

    private static final int BATCH_SIZE = 100;

    public GuildMemberDB(Database db) {
        this.db = db;
    }


    public void insertGuildMembers(List<GuildMember> data, String guildName) {

        EntityManagerFactory emf = db.getEntityManagerFactory();

        if (emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }


        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            String sql = "INSERT INTO `" + guildName + "` (name, score, parseTime) VALUES (:name, :score, :parseTime)";
            sqlBatch(data, sql, em);
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

    public GuildMember findGuildMemberPK(String name, LocalDateTime parseTime, String guildName) {
        EntityManagerFactory emf = db.getEntityManagerFactory();

        if (emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }

        EntityManager em = emf.createEntityManager();
        GuildMember member = null;
        try {
            String sql = "SELECT name, score, parseTime FROM `" + guildName + "` WHERE name = :name AND parseTime = :parseTime";
            // String sql = "SELECT * FROM `" + guildName + "` WHERE name = :name AND parseTime = :parseTime";
            // Query query = em.createNativeQuery(sql, GuildMember.class);
            Query query = em.createNativeQuery(sql);
            query.setParameter("name", name);
            query.setParameter("parseTime", parseTime);
            Object[] obj = (Object[]) query.getSingleResult();
            // member = new GuildMember((String)obj[0], (int)obj[1], (LocalDateTime)obj[2]);
            Timestamp timestamp = (Timestamp) obj[2];
            member = new GuildMember(
                (String)obj[0], (int)obj[1],
                timestamp.toLocalDateTime());
            // member = (GuildMember) query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return member;
    }

    public void deleteGuildMembers(List<GuildMember> data, String guildName) {
        EntityManagerFactory emf = db.getEntityManagerFactory();

        if (emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            String sql = "DELETE FROM `" + guildName + "` WHERE  name = :name AND score = :score AND parseTime = :parseTime";
            sqlBatch(data, sql, em);
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

    public void deleteGuildDataUntilDate(LocalDateTime date, String guildName) {
        EntityManagerFactory emf = db.getEntityManagerFactory();

        if (emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            String sql = "DELETE FROM `" + guildName + "` WHERE parseTime < :parseTime";
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

    private void sqlBatch(List<GuildMember> data, String sql, EntityManager em) {
        int sqlCount = 0;
        for (GuildMember member : data) {
            Query query = em.createNativeQuery(sql);
            query.setParameter("name", member.getName());
            query.setParameter("score", member.getScore());
            query.setParameter("parseTime", member.getParseTime());
            query.executeUpdate();

            sqlCount++;
            if (sqlCount % BATCH_SIZE == 0) {
                em.flush();
                em.clear();
                sqlCount = 0;
            }
        }
        if (sqlCount > 0) {
            em.flush();
            em.clear();
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