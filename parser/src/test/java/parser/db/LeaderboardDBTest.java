
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
import java.util.ArrayList;

import parser.entity.LeaderboardBaseEntity;
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

        List<LeaderboardBaseEntity> data = new ArrayList<LeaderboardBaseEntity>();
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

}