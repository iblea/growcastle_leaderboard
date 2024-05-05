
package parser.telegram;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import parser.db.Database;
import parser.entity.Token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class BotTest {

    @Test
    void createTest() {
        Bot bot = new Bot("telegram");
        assertThat(bot.getBotTokenName()).isEqualTo("telegram");
    }

    @Mock
    private Database dbMock;

    @Test
    void selectByBotNameTest() {
        Token token = new Token();
        token.setBotName("telegram");
        token.setBotToken("testToken");
        token.setBotChannel("testChannel");

        // Database dbMock = Mockito.spy(new Database("growcastle"));
        // BDDMockito.given(dbMock.selectByBotName("telegram")).willReturn(token);
        Mockito.when(dbMock.selectByBotName("telegram"))
            .thenReturn(token);

        Bot bot = new Bot("telegram");
        bot.getBotToken(dbMock);
        assertThat(bot.getToken()).isEqualTo(token);
    }

    @Test
    void selectByBotNameNullReturn() {
        // Database dbMock = Mockito.spy(new Database("growcastle"));
        // BDDMockito.given(dbMock.selectByBotName("telegram")).willReturn(token);
        Mockito.when(dbMock.selectByBotName("telegram"))
            .thenReturn(null);

        Bot bot = new Bot("telegram");
        assertThatThrownBy(() -> {
            bot.getBotToken(dbMock);
        })
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("telegram token is NULL");
    }

}