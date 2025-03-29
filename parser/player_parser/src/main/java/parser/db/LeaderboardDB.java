package parser.db;

import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import parser.entity.LeaderboardBaseEntity;
import parser.entity.LeaderboardGuild;
import parser.entity.MemberPK;
import parser.entity.LeaderboardPlayer;
import parser.entity.LeaderboardHell;
import parser.parser.LeaderboardType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 5분마다 800건
// 1일 = 24시간 = 1440분 = 288개
// 288 * 800 * 5

public class LeaderboardDB {

    private static Logger logger = LogManager.getLogger(LeaderboardDB.class);

    private static final int NO_JUMP = 5;
    private static final int HORN_JUMP = 6;
    private static final int DHORN_JUMP = 7;


    private Database db;
    private EntityManager em;

    public LeaderboardDB(Database db) {
        this.db = db;
        this.em = UtilDB.setEntityManager(this.db);
    }

    public void clearEntityManager() {
        if ( this.em != null && this.em.isOpen() ) {
            this.em.clear();
        }
    }

    public void closeEntityManager() {
        if ( UtilDB.closeEntityManager(this.em) ) {
            this.em = null;
        }
    }


    public boolean insertLeaderboards(List<LeaderboardBaseEntity> data, LeaderboardType type) {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return false;
        }
        EntityTransaction transaction = this.em.getTransaction();
        boolean result = true;
        try {
            transaction.begin();
            insertLeaderboardsByType(data, type);
            transaction.commit();
        } catch (Exception e) {
            logger.error("insertLeaderboards error");
            logger.error(e.getMessage());
            UtilDB.transactionRollback(transaction);
            result = false;
        }
        logger.debug("[{}][{}] leaderboards inserted", type.getTypename(), data.size());
        return result;
    }

    public boolean insertLeaderboardsPlayerTracking(List<LeaderboardBaseEntity> data, List<LeaderboardPlayer> agoWaveData) {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return false;
        }
        EntityTransaction transaction = this.em.getTransaction();
        boolean result = true;
        try {
            transaction.begin();
            insertsqlLeaderboardsPlayerTracking(data, agoWaveData);
            transaction.commit();
        } catch (Exception e) {
            logger.error("insertLeaderboardsPlayerTracking error");
            logger.error(e.getMessage());
            UtilDB.transactionRollback(transaction);
            result = false;
        }
        logger.debug("player [{}] leaderboards tracking inserted", data.size());
        return result;
    }

    private void insertLeaderboardsByType(List<LeaderboardBaseEntity> data, LeaderboardType type) {
        switch (type) {
            case PLAYER:
                for (LeaderboardBaseEntity leaderboard : data) {
                    this.em.persist(new LeaderboardPlayer(leaderboard));
                }
                break;
            case GUILD:
                for (LeaderboardBaseEntity leaderboard : data) {
                    this.em.persist(new LeaderboardGuild(leaderboard));
                }
                break;
            case HELL:
                for (LeaderboardBaseEntity leaderboard : data) {
                    this.em.persist(new LeaderboardHell(leaderboard));
                }
                break;
        }
    }

    private LeaderboardPlayer trackedAgoData(LeaderboardBaseEntity leaderboard, List<LeaderboardPlayer> agoWaveData) {
        LeaderboardPlayer player = new LeaderboardPlayer(leaderboard);
        boolean userFind = false;

        int index = -1;
        for (LeaderboardPlayer agoData : agoWaveData) {
            index++;

            if (! leaderboard.getName().equals(agoData.getLeaderboard().getName())) {
                continue;
            }

            userFind = true;

            // 웨이브 정보가 이전 데이터보다 작으면 시즌이 초기화된 것
            if (leaderboard.getScore() < agoData.getLeaderboard().getScore()) {
                player.setWaveTracketZero();
                agoWaveData.remove(index);
                break;
            }

            player.setAgoData(agoData);
            // 웨이브 정보가 같으면 이전 정보를 가져온다.
            if (leaderboard.getScore() == agoData.getLeaderboard().getScore()) {
                agoWaveData.remove(index);
                break;
            }

            int waveDiff = leaderboard.getScore() - agoData.getLeaderboard().getScore();
            player.addWave(1);
            if (waveDiff <= NO_JUMP) {
            } else if (waveDiff <= HORN_JUMP) {
                player.addHornJump(1);
            } else if (waveDiff <= DHORN_JUMP) {    // double horn jump
                player.addDHornJump(1);
            } else { //crystal jump
                player.addCrystalJump(1);
            }
            agoWaveData.remove(index);
            break;
        }

        // 유저를 찾지 못했으면 랭킹권에 새로 진입한 유저
        if (! userFind) {
            player.setWaveTracketZero();
        }

        return player;
    }

    private void insertsqlLeaderboardsPlayerTracking(List<LeaderboardBaseEntity> data, List<LeaderboardPlayer> agoWaveData) {
        if (agoWaveData == null) {
            logger.error("agoWaveData is null");
            return ;
        }
        for (LeaderboardBaseEntity leaderboard : data) {
            LeaderboardPlayer trackedPlayer = trackedAgoData(leaderboard, agoWaveData);
            this.em.persist(trackedPlayer);
        }
    }

    public void setInitializeTrackedData() {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return ;
        }
        EntityTransaction transaction = this.em.getTransaction();

        int updatedWave = 0;
        int updatedHornJump = 0;
        int updatedCrystalJump = 0;
        try {
            transaction.begin();
            String jpql = "UPDATE " + LeaderboardPlayer.class.getSimpleName() + " p SET p.wave = 0, p.hornJump = 0, p.dhornJump = 0, p.crystalJump = 0";
            Query query = this.em.createQuery(jpql);
            updatedWave = query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("setInitializeTrackedData error");
            logger.error(e.getMessage());
            UtilDB.transactionRollback(transaction);
        }
        logger.debug("[{}][{}][{}] updated tracked data set 0", updatedWave, updatedHornJump, updatedCrystalJump);
    }

    public void deleteLeaderboardsUntilDateWithType(LocalDateTime date, String tableName) {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return ;
        }
        EntityTransaction transaction = this.em.getTransaction();
        try {
            transaction.begin();
            String sql = "DELETE FROM " + tableName + " WHERE parseTime < :date";
            Query query = this.em.createNativeQuery(sql);
            query.setParameter("date", date);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("deleteLeaderboardsUntilDateWithType error");
            logger.error(e.getMessage());
            UtilDB.transactionRollback(transaction);
        }
        logger.debug("date : [{}], tableName : [{}]", date, tableName);
        logger.debug("deleteLeaderboardsUntilDateWithType success");
    }

    public void deleteAllQuery(String tableName) {
        String sql = "DELETE FROM " + tableName + "";
        Query query = this.em.createNativeQuery(sql);
        query.executeUpdate();
    }

    public void deleteLeaderboards(List<LeaderboardBaseEntity> data, LeaderboardType type) {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return ;
        }
        EntityTransaction transaction = this.em.getTransaction();
        try {
            transaction.begin();
            deleteLeaderboardsByType(data, type);
            transaction.commit();
        } catch (Exception e) {
            logger.error("deleteLeaderboards error");
            logger.error(e.getMessage());
            UtilDB.transactionRollback(transaction);
        }
        logger.debug("[{}][{}] leaderboards deleted", type.getTypename(), data.size());
    }

    private void deleteLeaderboardsByType(List<LeaderboardBaseEntity> data, LeaderboardType type) {
        switch (type) {
            case PLAYER:
                for (LeaderboardBaseEntity leaderboard : data) {
                    LeaderboardPlayer player = new LeaderboardPlayer(leaderboard);
                    this.em.remove(this.em.contains(player) ? player : this.em.merge(player));
                }
                break;
            case GUILD:
                for (LeaderboardBaseEntity leaderboard : data) {
                    LeaderboardGuild guild = new LeaderboardGuild(leaderboard);
                    this.em.remove(this.em.contains(guild) ? guild : this.em.merge(guild));
                }
                break;
            case HELL:
                for (LeaderboardBaseEntity leaderboard : data) {
                    LeaderboardHell hell = new LeaderboardHell(leaderboard);
                    this.em.remove(this.em.contains(hell) ? hell : this.em.merge(hell));
                }
                break;
        }
    }

    public LeaderboardBaseEntity findLeaderboardPK(String name, LocalDateTime parseTime, LeaderboardType type) {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return null;
        }
        MemberPK pk = new MemberPK(name, parseTime);
        Object leaderboard = null;
        try {
            switch (type) {
                case PLAYER:
                    leaderboard = this.em.find(LeaderboardPlayer.class, pk);
                    break;
                case GUILD:
                    leaderboard = this.em.find(LeaderboardGuild.class, pk);
                    break;
                case HELL:
                    leaderboard = this.em.find(LeaderboardHell.class, pk);
                    break;
            }
        } catch (Exception e) {
            logger.error("findLeaderboardPK error");
            logger.error(e.getMessage());
        }
        if (leaderboard == null) {
            return null;
        }

        switch (type) {
            case PLAYER:
                return ((LeaderboardPlayer) leaderboard).getLeaderboard();
            case GUILD:
                return ((LeaderboardGuild) leaderboard).getLeaderboard();
            case HELL:
                return ((LeaderboardHell) leaderboard).getLeaderboard();
        }
        return null;
    }

    public List<LeaderboardPlayer> getLeaderboardPlayersAll() {
        List<LeaderboardPlayer> leaderboardList = null;

        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return leaderboardList;
        }
        try {

            leaderboardList = this.em.createQuery(
                "SELECT lp FROM " + LeaderboardPlayer.class.getSimpleName() + " lp"
                , LeaderboardPlayer.class).getResultList();

        } catch (Exception e) {
            logger.error("getLeaderboardPlayersAll error");
            logger.error(e.getMessage());
            leaderboardList = null;
        }
        detachEntityPlayer(leaderboardList);
        return leaderboardList;
    }

    public void detachEntityPlayer(List<LeaderboardPlayer> leaderboardList) {
        if (leaderboardList == null) {
            return ;
        }
        for (LeaderboardPlayer leaderboard : leaderboardList) {
            em.detach(leaderboard);
        }
    }

    public boolean updateLeaderboards(List<LeaderboardBaseEntity> data, LeaderboardType type) {
        System.out.println("updateLeaderboards");
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return false;
        }

        if (data == null) {
            logger.error("data is null");
            return false;
        }
        if (data.isEmpty()) {
            logger.warn("data is empty");
            return false;
        }

        boolean result = true;
        EntityTransaction transaction = this.em.getTransaction();

        try {
            transaction.begin();
            deleteAllQuery(type.getRealTimeTableName());
            insertLeaderboardsByType(data, type);
            transaction.commit();
        } catch (Exception e) {
            logger.error("updateLeaderboards error");
            logger.error(e.getMessage());
            UtilDB.transactionRollback(transaction);
            result = false;
        }
        logger.debug("[{}][{}] leaderboards updated", type.getTypename(), data.size());
        return result;
    }

    public boolean updateLeaderboardsPlayerTracking(List<LeaderboardBaseEntity> data) {
        this.em = UtilDB.checkEntityManager(this.db, this.em);
        if (this.em == null) {
            logger.error("EntityManager is null");
            return false;
        }
        boolean result = true;
        EntityTransaction transaction = this.em.getTransaction();

        if (data == null) {
            logger.error("data is null");
            return false;
        }
        if (data.isEmpty()) {
            logger.warn("data is empty");
            return false;
        }

        try {
            transaction.begin();
            List<LeaderboardPlayer> agoWaveData = getLeaderboardPlayersAll();
            deleteAllQuery(LeaderboardType.PLAYER.getRealTimeTableName());
            insertsqlLeaderboardsPlayerTracking(data, agoWaveData);
            transaction.commit();
        } catch (Exception e) {
            logger.error("updateLeaderboardsPlayerTracking error");
            logger.error(e.getMessage());
            UtilDB.transactionRollback(transaction);
            result = false;
        }
        logger.debug("player [{}] leaderboardsPlayerTracking updated", data.size());
        return result;
    }


    public LocalDateTime getParseTime() {
        LocalDateTime now = LocalDateTime.now();
        int minutes = now.getMinute();
        int roundedMinutes = (minutes / 5) * 5;
        return now.withMinute(roundedMinutes)
            .withSecond(0)
            .withNano(0)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toLocalDateTime();
    }

    /**
     * 파싱한 현재 시간을 5분단위로 내려 Unix Time을 리턴한다. (Asia/Seoul - KST 기준)
     * @return long
     */
    public long getParseTimeUnix() {
        return getParseTime().atZone(ZoneId.of("Asia/Seoul")).toEpochSecond();
    }
}