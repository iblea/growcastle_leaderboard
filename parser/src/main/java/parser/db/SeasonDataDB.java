package parser.db;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import parser.entity.SeasonData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 5분마다 800건
// 1일 = 24시간 = 1440분 = 288개
// 288 * 800 * 5

public class SeasonDataDB {

    private static Logger logger = LogManager.getLogger(SeasonDataDB.class);

    private Database db;
    private EntityManager em;

    public SeasonDataDB(Database db) {
        this.db = db;
        this.em = UtilDB.setEntityManager(db);
    }

    public void clearEntityManager() {
        if ( this.em != null && this.em.isOpen() ) {
            this.em.clear();
        }
    }


    public boolean updateSeasonData(SeasonData data) {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return false;
        }
        EntityTransaction transaction = this.em.getTransaction();
        boolean result = true;
        try {
            transaction.begin();
            deleteQuery();
            this.em.persist(data);
            transaction.commit();
        } catch (Exception e) {
            logger.error("update SeaonData error");
            logger.error(e.getMessage());
            UtilDB.transactionRollback(transaction);
            result = false;
        }

        logger.debug("start [{}], end [{}] season data update", data.getStartDate(), data.getEndDate());
        return result;
    }

    private void deleteQuery() {
        String sql = "DELETE FROM SeasonData";
        Query query = this.em.createNativeQuery(sql);
        query.executeUpdate();
    }

    public boolean deleteSeasonData() {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return false;
        }
        EntityTransaction transaction = this.em.getTransaction();
        boolean result = true;

        try {
            transaction.begin();
            deleteQuery();
            transaction.commit();
        } catch (Exception e) {
            logger.error("delete Season Data error");
            logger.error(e.getMessage());
            UtilDB.transactionRollback(transaction);
            result = false;
        }

        logger.debug("season data delete");
        return result;

    }

    public SeasonData findSeasonData() {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return null;
        }
        List<SeasonData> dataList = null;
        try {
            TypedQuery<SeasonData> query = this.em.createQuery("SELECT s FROM SeasonData s", SeasonData.class);
            dataList = query.getResultList();
        } catch (Exception e) {
            logger.error("find SeasonData error");
            logger.error(e.getMessage());
            dataList = null;
        }

        if (dataList == null || dataList.isEmpty()) {
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