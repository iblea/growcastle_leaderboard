package parser.db;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Database {

    private static Logger logger = LogManager.getLogger(Database.class);

    private EntityManagerFactory emf;
    private String persistanceName;

    public Database(String persistanceName) {
        this.persistanceName = persistanceName;
        this.emf = null;
    }

    public String getPersistanceName() {
        return this.persistanceName;
    }

    public boolean connectEntityManagerFactory() {
        try {
            this.emf = Persistence.createEntityManagerFactory(this.persistanceName);
        } catch (PersistenceException e) {
            logger.error("cannot connect to Database");
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void disconnectEntityManagerFactory()
        throws NullPointerException {
        if (this.emf == null) {
            return ;
        }
        this.emf.close();
        this.emf = null;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return this.emf;
    }

}