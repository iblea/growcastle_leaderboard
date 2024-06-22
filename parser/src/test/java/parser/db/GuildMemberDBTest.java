package parser.db;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import parser.entity.GuildMember;


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
        GuildMemberDB dml = new GuildMemberDB(database);

        LocalDateTime parseTime = dml.getParseTime();
        GuildMember member = new GuildMember("Ib", 100000, parseTime);

        List<GuildMember> data = new ArrayList<GuildMember>();
        data.add(member);

        String guildName = "underdog";
        dml.insertGuildMembers(data, guildName);

        GuildMember findMember = dml.findGuildMemberPK("Ib", parseTime, guildName);


        assertThat(findMember).isNotNull();

        assertThatCode(() -> {
            assertThat(findMember.getName()).isEqualTo("Ib");
            assertThat(findMember.getScore()).isEqualTo(100000);
            assertThat(findMember.getParseTime()).isEqualTo(parseTime);
        }).doesNotThrowAnyException();


        dml.deleteGuildMembers(data, guildName);

        GuildMember findMemberAfterDelete = dml.findGuildMemberPK("Ib", parseTime, guildName);
        assertThat(findMemberAfterDelete).isNull();
    }
}