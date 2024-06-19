
package parser.db;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

import parser.entity.Leaderboard;


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

        Leaderboard leaderboard = new Leaderboard();

        String name = "test";
        LocalDateTime parseTime = dml.getParseTime();

        leaderboard.setRank(1);
        leaderboard.setScore(1000);
        leaderboard.setName(name);
        leaderboard.setParseTime(parseTime);
        // leaderboard.setParseTime(0);

        List<Leaderboard> data = new ArrayList<Leaderboard>();
        data.add(leaderboard);
        dml.insertLeaderboards(data, Optional.of("Player"));

        Leaderboard find = dml.findLeaderboardPK(name, parseTime);

        assertThatCode(() -> {
            assertThat(find.getRank()).isEqualTo(1);
            assertThat(find.getScore()).isEqualTo(1000);
            assertThat(find.getName()).isEqualTo(name);
            assertThat(find.getParseTime()).isEqualTo(parseTime);
        }).doesNotThrowAnyException();

        dml.deleteLeaderboards(data, Optional.of("Player"));
    }

}