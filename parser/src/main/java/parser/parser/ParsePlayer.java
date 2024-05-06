package parser.parser;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import parser.telegram.TelegramBot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import parser.entity.Player;

public class ParsePlayer extends ParseAPI {

    public ParsePlayer(TelegramBot bot) {
        super(bot);
    }

    public List<Player> playerJsonParser(String playerDataString)
        throws ParseException, NullPointerException, WrongJsonType
    {
        JSONArray playerlist = getAPIResponseData(playerDataString);

        List<Player> players = new ArrayList<Player>();
        for (int i = 0; i < playerlist.size(); i++) {
            JSONObject playerObject = (JSONObject)playerlist.get(i);

            Player player = new Player();
            Integer rank = (Integer)playerObject.get("rank");
            Integer score = (Integer)playerObject.get("score");
            player.setRank(rank.intValue());
            player.setName((String)playerObject.get("name"));
            player.setRank(score.intValue());
            players.add(player);
        }
        return players;
    }

    public List<Player> parsePlayer()
    {
        String playerURL = getCurrentKSTURL() + "/players";
        String playerData = null;
        try {
            playerData = requestURL(playerURL);
        } catch(Not200OK | IOException e) {
            e.printStackTrace();
            // alarm
            sendErrMsg(e.getMessage());
            return null;
        }

        List<Player> players = null;
        try {
            players = playerJsonParser(playerData);
        } catch (ParseException | NullPointerException | WrongJsonType e) {
            e.printStackTrace();
            sendErrMsg(e.getMessage());
            return null;
        }
        return players;
    }

}