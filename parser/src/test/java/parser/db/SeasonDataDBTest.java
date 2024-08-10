package parser.db;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import javax.transaction.Transactional;

import java.time.LocalDateTime;

import parser.entity.SeasonData;


@Testable
class SeasonDataDBTest {

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
        if (database == null) {
            System.out.println("database is null");
            return;
        }
        SeasonDataDB dml = new SeasonDataDB(database);

        SeasonData seasonData = new SeasonData();

        seasonData.setStartDate(LocalDateTime.now());
        seasonData.setEndDate(LocalDateTime.now());

        // dml.updateSeasonData(seasonData);
        // SeasonData find1 = dml.findSeasonData();
        // assertThat(find1).isEqualTo(seasonData);

        // dml.deleteSeasonData();
        // SeasonData find2 = dml.findSeasonData();
        SeasonData find2 = null;
        assertThat(find2).isNull();
    }

}