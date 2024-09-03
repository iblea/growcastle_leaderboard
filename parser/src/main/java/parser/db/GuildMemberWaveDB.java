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

    private Database db;
    private EntityManager em;

    public GuildMemberWaveDB(Database db) {
        this.db = db;
        this.em = UtilDB.setEntityManager(this.db);
    }

    public void clearEntityManager() {
        if ( this.em != null && this.em.isOpen() ) {
            this.em.clear();
        }
    }

    public boolean transactionRollback(EntityTransaction transaction) {
        boolean stat = true;
        try {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        } catch (Exception e) {
            logger.error("transactionRollback error");
            logger.error(e.getMessage());
            stat = false;
        }
        return stat;
    }

    public boolean insertGuildMemberWaves(List<GuildMemberWave> data) {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return false;
        }
        EntityTransaction transaction = this.em.getTransaction();
        boolean result = true;

        if (data.isEmpty()) {
            logger.info("insert data is empty");
            return false;
        }

        try {
            transaction.begin();
            insertGuildMemberWavesQuery(data);
            transaction.commit();
        } catch (Exception e) {
            logger.error("insertGuildMemberWave error");
            logger.error(e.getMessage());
            transactionRollback(transaction);
            result = false;
        }
        logger.debug("[{}] guildMemberWave inserted", data.size());
        return result;
    }

    private void insertGuildMemberWavesQuery(List<GuildMemberWave> data) {
        for (GuildMemberWave guildMemberWave : data) {
            this.em.persist(guildMemberWave);
        }
    }

    public void deleteGuildMemberWaveUntilDate(LocalDateTime date) {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return ;
        }
        EntityTransaction transaction = this.em.getTransaction();
        try {
            transaction.begin();
            String sql = "DELETE FROM guild_member_wave WHERE parseTime < :date";
            Query query = this.em.createNativeQuery(sql);
            query.setParameter("date", date);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("deleteGuildMemberWaveUntilDate error");
            logger.error(e.getMessage());
            transactionRollback(transaction);
        }
        logger.debug("date : [{}] deleteGuildMemberWaveUntilDate success", date);
    }

    public void deleteAllQuery() {
        String sql = "DELETE FROM guild_member_wave";
        Query query = this.em.createNativeQuery(sql);
        query.executeUpdate();
    }

    public void deleteGuildMemberWaves(List<GuildMemberWave> data) {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return ;
        }
        EntityTransaction transaction = this.em.getTransaction();
        try {
            transaction.begin();
            for (GuildMemberWave guildMember : data) {
                this.em.remove(em.contains(guildMember) ? guildMember : this.em.merge(guildMember));
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("deleteGuildMemberWaves error");
            logger.error(e.getMessage());
            transactionRollback(transaction);
        }
        logger.debug("[{}] guidlMemberWave deleted", data.size());
    }


    public GuildMemberWave findGuildMemberWavePK(String name, LocalDateTime parseTime) {
        MemberPK pk = new MemberPK(name, parseTime);
        Object data = null;
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return null;
        }
        try {
            data = this.em.find(GuildMemberWave.class, pk);
        } catch (Exception e) {
            logger.error("findGuildMemberWavePK error");
            logger.error(e.getMessage());
            data = null;
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