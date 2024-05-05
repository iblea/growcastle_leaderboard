
package parser.db;

import org.junit.jupiter.api.Test;

import javax.persistence.PersistenceException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;


class DatabaseTest {
    @Test
    void connectTest() {
        Database database = new Database("growcastle");
        assertThat(database.getPersistanceName()).isEqualTo("growcastle");

        assertThatCode((() -> {
            database.connectEntityManagerFactory();
            database.disconnectEntityManagerFactory();
        }))
        .doesNotThrowAnyException();
    }

    @Test
    void connectWrongPersistanceTest() {
        Database database = new Database("no");

        assertThatThrownBy(() -> {
            database.connectEntityManagerFactory();
        })
        .isExactlyInstanceOf(PersistenceException.class);
    }

}