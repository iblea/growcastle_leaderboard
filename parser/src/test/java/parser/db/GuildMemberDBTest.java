package parser.db;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import parser.entity.GuildMemberWave;


@Testable
class GuildMemberDBTest {
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
        GuildMemberWaveDB dml = new GuildMemberWaveDB(database);
        LocalDateTime parseTime = LocalDateTime.of(2017, 01, 01, 00, 00, 00);
        GuildMemberWave member = new GuildMemberWave("Ib", "underdog", 100000, parseTime, "2017-01-01");

        List<GuildMemberWave> data = new LinkedList<GuildMemberWave>();
        data.add(member);
        dml.insertGuildMemberWaves(data);

        GuildMemberWave findMember1 = dml.findGuildMemberWavePK("Ib", parseTime);
        assertThat(findMember1).isNotNull();
        // assertThat(findMember1).isNull();
        assertThatCode(() -> {
            assertThat(findMember1.getName()).isEqualTo("Ib");
            assertThat(findMember1.getScore()).isEqualTo(100000);
            assertThat(findMember1.getParseTime()).isEqualTo(parseTime);
        }).doesNotThrowAnyException();

        dml.deleteGuildMemberWaves(data);
        GuildMemberWave findMember2 = dml.findGuildMemberWavePK("Ib", parseTime);
        assertThat(findMember2).isNull();
    }

}