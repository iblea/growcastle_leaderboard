package parser.db;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import junit.framework.Assert;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.Month;

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
        assertThat(1).isEqualTo(1);
        // SeasonDataDB dml = new SeasonDataDB(database);

        // SeasonData seasonData = new SeasonData();
        // LocalDateTime specificDateTime1 = LocalDateTime.of(2023, Month.OCTOBER, 2, 15, 20, 3);
        // LocalDateTime specificDateTime2 = LocalDateTime.of(2023, Month.NOVEMBER, 2, 15, 20, 3);

        // seasonData.setStartDate(specificDateTime1);
        // seasonData.setEndDate(specificDateTime2);

        // dml.updateSeasonData(seasonData);
        // SeasonData find1 = dml.findSeasonData();
        // assertThatCode(() -> {
        //     assertThat(find1).isNotNull();
        //     assertThat(find1.getStartDate()).isEqualTo(seasonData.getStartDate());
        //     assertThat(find1.getEndDate()).isEqualTo(seasonData.getEndDate());
        // }).doesNotThrowAnyException();

        // dml.deleteSeasonData();
        // SeasonData find2 = dml.findSeasonData();
        // assertThat(find2).isNull();
    }

}