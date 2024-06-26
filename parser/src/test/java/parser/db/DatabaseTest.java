
package parser.db;

import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.mockito.Mockito;

// import javax.persistence.PersistenceException;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;


class DatabaseTest {

    @Test
    void connectTest() {
        // DB가열려 있어야 한다.
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

        assertThat(database.connectEntityManagerFactory()).isFalse();
        // assertThatThrownBy(() -> {
        //     database.connectEntityManagerFactory();
        // })
        // .isExactlyInstanceOf(PersistenceException.class);
    }

}