package parser.db;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import parser.entity.SeasonData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 5분마다 800건
// 1일 = 24시간 = 1440분 = 288개
// 288 * 800 * 5

public class SeasonDataDB {

    private static Logger logger = LogManager.getLogger(SeasonDataDB.class);

    Database db;

    public SeasonDataDB(Database db) {
        this.db = db;
    }

    private EntityManager makeTransaction() {
        EntityManagerFactory emf = db.getEntityManagerFactory();
        if (emf == null) {
            logger.error("EntityManagerFactory is null");
            throw new NullPointerException("EntityManagerFactory is null");
        }
        return emf.createEntityManager();
    }

    public boolean updateSeasonData(SeasonData data) {
        EntityManager em = makeTransaction();
        EntityTransaction transaction = em.getTransaction();
        boolean result = true;
        try {
            transaction.begin();
            deleteQuery(em);
            em.persist(data);
            transaction.commit();
        } catch (Exception e) {
            logger.error("update SeaonData error");
            logger.error(e.getMessage());
            if (transaction.isActive()) {
                transaction.rollback();
            }
            result = false;
        } finally {
            em.close();
        }

        logger.debug("start [{}], end [{}] season data update", data.getStartDate(), data.getEndDate());
        return result;
    }

    private void deleteQuery(EntityManager em) {
        String sql = "DELETE FROM `SeasonData` WHERE parseTime < :date";
        Query query = em.createNativeQuery(sql);
        query.executeUpdate();
    }

    public boolean deleteSeasonData() {
        EntityManager em = makeTransaction();
        EntityTransaction transaction = em.getTransaction();
        boolean result = true;

        try {
            transaction.begin();
            deleteQuery(em);
            transaction.commit();
        } catch (Exception e) {
            logger.error("delete Season Data error");
            logger.error(e.getMessage());
            if (transaction.isActive()) {
                transaction.rollback();
            }
            result = false;
        } finally {
            em.close();
        }

        logger.debug("season data delete");
        return result;

    }

    public SeasonData findSeasonData() {
        EntityManager em = makeTransaction();

        List<SeasonData> dataList = null;
        try {
            dataList = em.createQuery("SELECT s FROM SeasonData s", SeasonData.class).getResultList();
        } catch (Exception e) {
            logger.error("find SeasonData error");
            logger.error(e.getMessage());
            dataList = null;
        } finally {
            em.close();
        }

        if (dataList == null || dataList.size() == 0) {
            logger.info("SeasonData is empty");
            return null;
        }

        if (dataList.size() > 1) {
            logger.error("SeasonData is more than 1");
            return null;
        }

        return dataList.get(0);
    }

}