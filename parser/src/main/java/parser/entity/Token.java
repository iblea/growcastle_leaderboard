package parser.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "TOKEN")
public class Token {

    @Id
    @Column(name = "bot_name")
    private String botName;

    @Column(name = "bot_token")
    private String botToken;

    @Column(name = "bot_channel")
    private String botChannel;

    public String getBotName() {
        return this.botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getBotToken() {
        return this.botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getBotChannel() {
        return this.botChannel;
    }

    public void setBotChannel(String botChannel) {
        this.botChannel = botChannel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final Token token = (Token)obj;
        if (this.getBotName() != token.getBotName()) {
            return false;
        }

        if (this.getBotToken() != token.getBotToken()) {
            return false;
        }

        if (this.getBotChannel() != token.getBotChannel()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.botName,
            this.botToken,
            this.botChannel
        );
    }


}