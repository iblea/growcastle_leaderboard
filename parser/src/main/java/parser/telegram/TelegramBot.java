package parser.telegram;

import parser.db.Database;
import parser.entity.Token;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class TelegramBot implements LongPollingSingleThreadUpdateConsumer {

    private static Logger logger = LogManager.getLogger(TelegramBot.class);

    // private final String BOTNAME = "Telegram Alarm Bot";
    private Token token;
    private long defaultChannel;
    private TelegramClient telegramClient;

    public TelegramBot() {
        this.token = null;
        this.defaultChannel = -1;
        this.telegramClient = null;
    }

    public TelegramBot(Database db, Optional<String> botTokenName) {
        this.token = null;
        this.defaultChannel = -1;
        this.telegramClient = null;
        this.setBotToken(db, botTokenName);
    }


    public Token getToken() {
        return this.token;
    }

    public String getAPIBotToken() {
        if (this.token == null) {
            return null;
        }
        return this.token.getBotToken();
    }


    @Override
    public void consume(Update update) {
        // We check if the update has a message and the message has text
        // if (update.hasMessage() && update.getMessage().hasText()) {
        //     // Set variables
        //     String message_text = update.getMessage().getText();
        //     long chat_id = update.getMessage().getChatId();
        //     sendMsg(chat_id, message_text);
        // }
    }

    public void sendMsg(String msg) {
        this.sendMsg(this.defaultChannel, msg);
    }

    public void sendMsg(long chatId, String msg) {
        SendMessage message = SendMessage // Create a message object
            .builder()
            .chatId(chatId)
            .text(msg)
            .build();
        try {
            this.telegramClient.execute(message); // Sending our message object to user
        } catch (TelegramApiException | NullPointerException e) {
            logger.error("Error sendMsg telegram");
            logger.error(e.getMessage());
        }
    }

    /**
     * db에서 botTokenName에 대한 token 정보를 가져온다.
     * botToken 정보에는 token, 해당 bot의 메시지 channel id가 존재한다.
     * @param db
     * @param botTokenName
     */
    public void setBotToken(Database db, Optional<String> botTokenName) {

        if (botTokenName.isPresent() == false) {
            logger.error("botTokenName is NULL");
            throw new NullPointerException("botTokenName is NULL");
        }

        this.token = this.selectByBotName(db, botTokenName.get());

        if (token == null) {
            logger.error("[{}] token is NULL", botTokenName.get());
            throw new NullPointerException(botTokenName.get() + " token is NULL");
        }

        this.telegramClient = new OkHttpTelegramClient(this.getAPIBotToken());
        this.defaultChannel = Long.valueOf(this.token.getBotChannel());
    }

    public Token selectByBotName(Database db, String botName) {
        EntityManagerFactory emf = db.getEntityManagerFactory();
        if (emf == null) {
            logger.error("EntityManagerFactory is null");
            throw new NullPointerException("EntityManagerFactory is null");
        }

        EntityManager em = emf.createEntityManager();
        // EntityTransaction tx = em.getTransaction();
        // tx.begin();
        Token tok = null;
        try {
            tok = em.find(Token.class, botName);
            // tx.commit();
        } catch (Exception e) {
            logger.error("Error in selectByBotName.");
            logger.error("{} is not found in Token table.", botName);
            logger.error(e.getMessage());
            tok = null;
        } finally {
            em.close();
        }
        return tok;
    }


}
