
package parser.telegram;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import parser.db.Database;
import parser.entity.Token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class TelegramBotTest {

    @Mock
    private Database dbMock;

    @Test
    void selectByBotNameTest() {
        Token token = new Token();
        token.setBotName("telegram");
        token.setBotToken("testToken");
        token.setBotChannel("123456");

        // Database dbMock = Mockito.spy(new Database("growcastle"));
        // BDDMockito.given(dbMock.getEntityManagerFactory()).willReturn(null);
        // Mockito.when(dbMock.getEntityManagerFactory())
        //     .thenReturn(null);


        TelegramBot bot = spy(TelegramBot.class);
        // BDDMockito.given(bot.selectByBotName(dbMock, Optional.of("telegram"))).willReturn(token);
        // Mockito.when(bot.selectByBotName()).thenReturn(token);
        Mockito.doReturn(token).when(bot).selectByBotName(dbMock, Optional.of("telegram"));

        // TelegramBot bot = new TelegramBot();
        bot.setBotToken(dbMock, Optional.of("telegram"));
        assertThat(bot.getToken()).isEqualTo(token);
    }

    @Test
    void selectByBotNameNullReturn() {

        TelegramBot bot = spy(TelegramBot.class);
        Mockito.doReturn(null).when(bot).selectByBotName(dbMock, Optional.of("telegram"));

        // TelegramBot bot = new TelegramBot();
        assertThatThrownBy(() -> {
            bot.setBotToken(dbMock, Optional.of("telegram"));
        })
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("telegram token is NULL");
    }



}