package parser.db;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UtilDB {

    private static Logger logger = LogManager.getLogger(UtilDB.class);

    public static EntityManager setEntityManager(Database db) {
        try {
            EntityManagerFactory emf = db.getEntityManagerFactory();
            return emf.createEntityManager();
        } catch (Exception e) {
            logger.error("setEntityManager error");
            logger.error(e.getMessage());
        }
        return null;
    }

    public static EntityManager checkEntityManager(Database db, EntityManager em) {
        if (em == null) {
            logger.debug("EntityManager is null");
            return setEntityManager(db);
        }
        if (!em.isOpen()) {
            logger.debug("EntityManager is closed");
            return setEntityManager(db);
        }
        return em;
    }

    public static boolean closeEntityManager(EntityManager em) {
        if (em == null) {
            logger.info("EntityManager is null");
            return false;
        }
        try {
            if (em.isOpen()) {
                em.close();
            }
        } catch (Exception e) {
            logger.error("closeEntityManager error");
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean transactionRollback(EntityTransaction transaction) {
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

}