package parser.telegram;

import parser.db.Database;
import parser.entity.Token;

public class Bot {
    private String botTokenName;
    private Token token;

    public Bot(String botTokenName) {
        this.botTokenName = botTokenName;
        this.token = null;
    }

    public String getBotTokenName() {
        return this.botTokenName;
    }

    public Token getToken() {
        return this.token;
    }

    public void getBotToken(Database db) {
        this.token = db.selectByBotName(this.botTokenName);

        if (token == null) {
            System.out.println("token is NULL");
            throw new NullPointerException(this.botTokenName + " token is NULL");
        }
    }


}