package parser.db;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

public class Database {
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
            e.printStackTrace();
            System.out.println("Error: cannot connect to Database");
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