package parser.parser;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import parser.telegram.TelegramBot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import parser.entity.Leaderboard;

public class ParseLeaderboard extends ParseAPI {

    public ParseLeaderboard(TelegramBot bot) {
        super(bot);
    }

    public List<Leaderboard> leaderboardJsonParser(String leaderboardDataString)
        throws ParseException, NullPointerException, WrongJsonType
    {
        JSONArray leaderboardlist = getAPIResponseData(leaderboardDataString);

        List<Leaderboard> leaderboards = new ArrayList<Leaderboard>();
        for (int i = 0; i < leaderboardlist.size(); i++) {
            JSONObject rankObject = (JSONObject)leaderboardlist.get(i);

            Leaderboard leaderboard = new Leaderboard();
            Integer rank = (Integer)rankObject.get("rank");
            Integer score = (Integer)rankObject.get("score");
            leaderboard.setRank(rank.intValue());
            leaderboard.setName((String)rankObject.get("name"));
            leaderboard.setRank(score.intValue());
            leaderboards.add(leaderboard);
        }
        return leaderboards;
    }

    public List<Leaderboard> parseLeaderboards(String leaderboardType)
    {
        // players, guilds
        String leaderboardURL = getCurrentKSTURL() + "/" + leaderboardType;
        String leaderboardData = null;
        try {
            leaderboardData = requestURL(leaderboardURL);
        } catch(Not200OK | IOException e) {
            e.printStackTrace();
            // alarm
            sendErrMsg(e.getMessage());
            return null;
        }

        List<Leaderboard> leaderboards = null;
        try {
            leaderboards = leaderboardJsonParser(leaderboardData);
        } catch (ParseException | NullPointerException | WrongJsonType e) {
            e.printStackTrace();
            sendErrMsg(e.getMessage());
            return null;
        }
        return leaderboards;
    }

}