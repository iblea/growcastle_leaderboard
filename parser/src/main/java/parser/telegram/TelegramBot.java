package parser.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import parser.db.Database;
import parser.entity.Token;

public class TelegramBot extends TelegramLongPollingBot {

    TelegramBotsApi botsApi;
    private Token token = null;

    // https://jsonobject.tistory.com/404
    @Override
    public void onUpdateReceived(Update update) {
    }

    public synchronized void sendMsg(String s) {
        SendMessage sm = new SendMessage();
        sm.setChatId(token.getBotChannel());
        sm.setText(s);
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            System.out.println("Exception: " + e.toString());
        }
    }

    /**
     * db에서 botTokenName에 대한 token 정보를 가져온다.
     * botToken 정보에는 token, 해당 bot의 메시지 channel id가 존재한다.
     * @param db
     * @param botTokenName
     */
    public void setBotToken(Database db, String botTokenName) {
        this.token = db.selectByBotName(botTokenName);

        if (token == null) {
            System.out.println("token is NULL");
            throw new NullPointerException(botTokenName + " token is NULL");
        }
    }

    public Token getToken() {
        return this.token;
    }

    @Override
    public String getBotUsername() {
        return "GrowCastle Alarm Bot";
    }

    @Override
    public String getBotToken() {
        return this.token.getBotToken();
    }

    public void connectBot()
        throws NullPointerException, TelegramApiException
    {
        if (this.token == null) {
            throw new NullPointerException();
        }

        this.botsApi = new TelegramBotsApi(DefaultBotSession.class);
        this.botsApi.registerBot(this);
    }
}
