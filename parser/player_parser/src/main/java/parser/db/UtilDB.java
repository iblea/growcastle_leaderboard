package parser.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

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
            logger.debug("EntityManager is null 1");
            return setEntityManager(db);
        }
        if (!em.isOpen()) {
            logger.debug("EntityManager is closed");
            return setEntityManager(db);
        }
        return em;
    }

    public static boolean closeEntityManager(EntityManager em) {
        // System.out.println("closeEntityManager");
        if (em == null) {
            logger.info("EntityManager is null 2");
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