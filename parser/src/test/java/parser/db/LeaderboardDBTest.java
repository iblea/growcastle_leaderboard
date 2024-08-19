
package parser.db;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.util.List;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedList;

import parser.entity.LeaderboardBaseEntity;
import parser.entity.LeaderboardPlayer;
import parser.parser.LeaderboardType;


@Testable
class LeaderboardDBTest {

    Database database = null;

    @BeforeEach
    void setup() {
        // Code to run before each test case
        System.out.println("Setup InsertTest");
        database = new Database("growcastle");
        database.connectEntityManagerFactory();
    }

    @AfterEach
    void tearDown() {
        // Code to run after each test case
        database.disconnectEntityManagerFactory();
    }

    @Test
    @Transactional
    void InsertOne() {
        LeaderboardDB dml = new LeaderboardDB(database);

        LeaderboardBaseEntity leaderboard = new LeaderboardBaseEntity();

        String name = "test";
        LocalDateTime parseTime = dml.getParseTime();

        leaderboard.setRank(1);
        leaderboard.setScore(1000);
        leaderboard.setName(name);
        leaderboard.setParseTime(parseTime);
        // leaderboard.setParseTime(0);

        List<LeaderboardBaseEntity> data = new LinkedList<>();
        data.add(leaderboard);
        dml.insertLeaderboards(data, LeaderboardType.PLAYER);

        LeaderboardBaseEntity find = dml.findLeaderboardPK(name, parseTime, LeaderboardType.PLAYER);

        assertThatCode(() -> {
            assertThat(find.getRank()).isEqualTo(1);
            assertThat(find.getScore()).isEqualTo(1000);
            assertThat(find.getName()).isEqualTo(name);
            assertThat(find.getParseTime()).isEqualTo(parseTime);
        }).doesNotThrowAnyException();

        dml.deleteLeaderboards(data, LeaderboardType.PLAYER);

        LeaderboardBaseEntity remove_find = dml.findLeaderboardPK(name, parseTime, LeaderboardType.PLAYER);
        assertThat(remove_find).isNull();
    }


    @Test
    @Transactional
    void InsertThree() {
        LeaderboardDB dml = new LeaderboardDB(database);

        LocalDateTime parseTime = dml.getParseTime();
        LeaderboardBaseEntity leaderboard1 = new LeaderboardBaseEntity(1, "__________test10__________", 1000, parseTime);
        LeaderboardBaseEntity leaderboard2 = new LeaderboardBaseEntity(2, "__________test20__________", 900, parseTime);
        LeaderboardBaseEntity leaderboard3 = new LeaderboardBaseEntity(3, "__________test30__________", 800, parseTime);
        LeaderboardBaseEntity leaderboard4 = new LeaderboardBaseEntity(4, "__________test40__________", 500, parseTime);

        // insert 이전 조회
        List<LeaderboardPlayer> selectList = dml.getLeaderboardPlayersAll();
        assertThat(selectList).isNotNull();
        int insert_ago_count = selectList.size();
        // List<LeaderboardPlayer> selectList = null;
        // int insert_ago_count = 0;

        // insert
        List<LeaderboardBaseEntity> data = new LinkedList<>();
        data.add(leaderboard1);
        data.add(leaderboard2);
        data.add(leaderboard3);
        data.add(leaderboard4);
        dml.insertLeaderboards(data, LeaderboardType.PLAYER);

        // insert 이후 조회
        // insert 이후 데이터 확인
        selectList = dml.getLeaderboardPlayersAll();
        assertThat(selectList).isNotNull();

        int insert_after_count = selectList.size();
        int insert_count = insert_after_count - insert_ago_count;
        assertThat(insert_count).isEqualTo(data.size());

        for (int i = 0; i < selectList.size(); i++) {
            LeaderboardBaseEntity item = selectList.get(i);
            if (item.getName().equals("__________test10__________") ||
                item.getName().equals("__________test20__________") ||
                item.getName().equals("__________test30__________")) {
                selectList.remove(i);
                --i;
            }
        }

        // 삭제된 이후 카운트
        int item_remove_after_size = selectList.size();
        //  삭제된 카운트 획득
        int item_removed_count = insert_after_count - item_remove_after_size;
        assertThat(item_removed_count).isEqualTo(3);

        dml.deleteLeaderboards(data, LeaderboardType.PLAYER);

        selectList = dml.getLeaderboardPlayersAll();
        int remove_after_count = selectList.size();
        assertThat(remove_after_count).isEqualTo(insert_ago_count);

    }

    @Test
    @Transactional
    void TrackedInitializeTest() {
        LeaderboardDB dml = new LeaderboardDB(database);
        LocalDateTime parseTime = dml.getParseTime();

        LeaderboardBaseEntity leaderboard1 = new LeaderboardBaseEntity(1, "__________test1__________", 1000, parseTime);
        LeaderboardBaseEntity leaderboard2 = new LeaderboardBaseEntity(2, "__________test2__________", 900, parseTime);
        LeaderboardBaseEntity leaderboard3 = new LeaderboardBaseEntity(3, "__________test3__________", 800, parseTime);
        LeaderboardBaseEntity leaderboard4 = new LeaderboardBaseEntity(4, "__________test4__________", 500, parseTime);


        // insert 이전 조회
        List<LeaderboardPlayer> selectList = dml.getLeaderboardPlayersAll();
        assertThat(selectList).isNotNull();
        int insert_ago_count = selectList.size();

        // insert
        List<LeaderboardBaseEntity> data_init = new LinkedList<>();
        data_init.add(leaderboard1);
        data_init.add(leaderboard2);
        data_init.add(leaderboard3);
        data_init.add(leaderboard4);

        dml.insertLeaderboards(data_init, LeaderboardType.PLAYER);


        // insert 이후 조회
        selectList = dml.getLeaderboardPlayersAll();
        assertThat(selectList).isNotNull();
        int insert_after_count = selectList.size();
        int inserted_count = insert_after_count - insert_ago_count;
        assertThat(inserted_count).isEqualTo(data_init.size());


        for (LeaderboardPlayer item : selectList) {
            if (item.getName().equals("__________test1__________") ||
                item.getName().equals("__________test2__________") ||
                item.getName().equals("__________test3__________") ||
                item.getName().equals("__________test4__________")
            ) {
                assertThat(item.getWave()).isZero();
                assertThat(item.getHornJump()).isZero();
                assertThat(item.getCrystalJump()).isZero();
            }
        }

        List<LeaderboardBaseEntity> data_waving = new LinkedList<>();
        // no jump
        leaderboard1.setScore(leaderboard1.getScore() + 5);
        // horn jump
        leaderboard2.setScore(leaderboard2.getScore() + 6);
        // double horn jump
        leaderboard3.setScore(leaderboard3.getScore() + 7);
        // crystal jump
        leaderboard4.setScore(leaderboard3.getScore() + 30);
        data_waving.add(leaderboard1);
        data_waving.add(leaderboard2);
        data_waving.add(leaderboard3);
        data_waving.add(leaderboard4);

        // dml.updateLeaderboardsPlayerTracking(data);
        List<LeaderboardPlayer> agoWaveData = dml.getLeaderboardPlayersAll();
        dml.deleteLeaderboards(data_waving, LeaderboardType.PLAYER);
        dml.insertLeaderboardsPlayerTracking(data_waving, agoWaveData);


        // wave jump tracking
        selectList = dml.getLeaderboardPlayersAll();
        // insert 이후 조회
        assertThat(selectList).hasSize(insert_after_count);

        for (LeaderboardPlayer item : selectList) {
            if (item.getName().equals("__________test1__________")) {
                assertThat(item.getWave()).isEqualTo(1);
                assertThat(item.getHornJump()).isZero();
                assertThat(item.getDHornJump()).isZero();
                assertThat(item.getCrystalJump()).isZero();
            } else if (item.getName().equals("__________test2__________")) {
                assertThat(item.getWave()).isEqualTo(1);
                assertThat(item.getHornJump()).isEqualTo(1);
                assertThat(item.getDHornJump()).isZero();
                assertThat(item.getCrystalJump()).isZero();
            } else if (item.getName().equals("__________test3__________")) {
                assertThat(item.getWave()).isEqualTo(1);
                assertThat(item.getHornJump()).isZero();
                assertThat(item.getDHornJump()).isEqualTo(1);
                assertThat(item.getCrystalJump()).isZero();
            } else if (item.getName().equals("__________test4__________")) {
                assertThat(item.getWave()).isEqualTo(1);
                assertThat(item.getHornJump()).isZero();
                assertThat(item.getDHornJump()).isZero();
                assertThat(item.getCrystalJump()).isEqualTo(1);
            }
        }

        // init wave jump tracking
        dml.setInitializeTrackedData();
        selectList = dml.getLeaderboardPlayersAll();
        List<LeaderboardBaseEntity> deleteItem = new LinkedList<>();
        for (LeaderboardPlayer item : selectList) {
            if (item.getName().equals("__________test1__________") ||
                item.getName().equals("__________test2__________") ||
                item.getName().equals("__________test3__________") ||
                item.getName().equals("__________test4__________")
            ) {
                assertThat(item.getWave()).isZero();
                assertThat(item.getHornJump()).isZero();
                assertThat(item.getDHornJump()).isZero();
                assertThat(item.getCrystalJump()).isZero();
                deleteItem.add(item.getLeaderboard());
            }
        }
        dml.deleteLeaderboards(deleteItem, LeaderboardType.PLAYER);

        // dml.deleteLeaderboards(data_waving, LeaderboardType.PLAYER);


        selectList = dml.getLeaderboardPlayersAll();
        assertThat(selectList).hasSize(insert_ago_count);


    }


}