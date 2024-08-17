package parser.db;

import java.util.List;
import java.time.LocalDateTime;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import parser.entity.GuildMemberWave;
import parser.entity.MemberPK;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 5분마다 800건
// 1일 = 24시간 = 1440분 = 288개
// 288 * 800 * 5

public class GuildMemberWaveDB {

    private static Logger logger = LogManager.getLogger(GuildMemberWaveDB.class);

    Database db;

    public GuildMemberWaveDB(Database db) {
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

    public boolean insertGuildMemberWaves(List<GuildMemberWave> data) {
        EntityManager em = makeTransaction();
        EntityTransaction transaction = em.getTransaction();
        boolean result = true;

        if (data.isEmpty()) {
            logger.info("insert data is empty");
            return false;
        }

        try {
            transaction.begin();
            insertGuildMemberWavesQuery(data, em);
            transaction.commit();
        } catch (Exception e) {
            logger.error("insertGuildMemberWave error");
            logger.error(e.getMessage());
            if (transaction.isActive()) {
                transaction.rollback();
            }
            result = false;
        } finally {
            em.close();
        }
        logger.debug("[{}] guildMemberWave inserted", data.size());
        return result;
    }

    private void insertGuildMemberWavesQuery(List<GuildMemberWave> data, EntityManager em) {
        for (GuildMemberWave guildMemberWave : data) {
            em.persist(guildMemberWave);
        }
    }

    public void deleteGuildMemberWaveUntilDate(LocalDateTime date) {
        EntityManager em = makeTransaction();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            String sql = "DELETE FROM guild_member_wave WHERE parseTime < :date";
            Query query = em.createNativeQuery(sql);
            query.setParameter("date", date);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("deleteGuildMemberWaveUntilDate error");
            logger.error(e.getMessage());
            if (transaction.isActive()) {
                transaction.rollback();
            }
        } finally {
            em.close();
        }
        logger.debug("date : [{}] deleteGuildMemberWaveUntilDate success", date);
    }

    public void deleteAllQuery(EntityManager em) {
        String sql = "DELETE FROM guild_member_wave";
        Query query = em.createNativeQuery(sql);
        query.executeUpdate();
    }

    public void deleteGuildMemberWaves(List<GuildMemberWave> data) {
        EntityManager em = makeTransaction();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            for (GuildMemberWave guildMember : data) {
                em.remove(em.contains(guildMember) ? guildMember : em.merge(guildMember));
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("deleteGuildMemberWaves error");
            logger.error(e.getMessage());
            if (transaction.isActive()) {
                transaction.rollback();
            }
        } finally {
            em.close();
        }
        logger.debug("[{}] guidlMemberWave deleted", data.size());
    }


    public GuildMemberWave findGuildMemberWavePK(String name, LocalDateTime parseTime) {
        MemberPK pk = new MemberPK(name, parseTime);
        Object data = null;
        EntityManager em = makeTransaction();
        try {
            data = em.find(GuildMemberWave.class, pk);
        } catch (Exception e) {
            logger.error("findGuildMemberWavePK error");
            logger.error(e.getMessage());
            data = null;
        } finally {
            em.close();
        }
        if (data == null) {
            return null;
        }

        return (GuildMemberWave) data;
    }

    public int getMinUnit(LocalDateTime timeobj) {
        int minute = timeobj.getMinute();
        return (minute / 15) * 15;
    }


}