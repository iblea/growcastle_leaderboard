package parser.db;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

public class Database {
    private EntityManagerFactory emf;
    private String persistance_name;

    public Database(String persistance_name) {
        this.persistance_name = persistance_name;
        this.emf = null;
    }

    public String getPersistanceName() {
        return this.persistance_name;
    }

    public void connectEntityManagerFactory()
        throws PersistenceException {
        this.emf = Persistence.createEntityManagerFactory(this.persistance_name);
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