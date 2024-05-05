package parser.db;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import parser.entity.Token;

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

    public Token selectByBotName(String botName) {
        if (this.emf == null) {
            throw new NullPointerException("EntityManagerFactory is null");
        }

        EntityManager em = emf.createEntityManager();
        // EntityTransaction tx = em.getTransaction();
        // tx.begin();
        Token token = null;
        try {
            token = em.find(Token.class, botName);
            // tx.commit();
        } catch (Exception e) {
            // tx.rollback();
        } finally {
            em.close();
        }
        return token;
    }
}