package parser.parser;

import java.io.IOException;
import java.util.List;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import parser.telegram.TelegramBot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import parser.entity.Leaderboard;


public class ParseGuild extends ParseAPI {

    public ParseGuild(TelegramBot bot) {
        super(bot);
    }

    public List<Leaderboard> guildJsonParser(String leaderboardDataString)
        throws ParseException, NullPointerException, WrongJsonType
    {
        JSONArray leaderboardlist = getAPIResponseData(leaderboardDataString);

        List<Leaderboard> leaderboards = new ArrayList<Leaderboard>();
        for (int i = 0; i < leaderboardlist.size(); i++) {
            JSONObject rankObject = (JSONObject)leaderboardlist.get(i);

            // Guild의 Player 정보에는 길드별 Rank에 대한 정보가 없다.
            Integer score = (Integer)rankObject.get("score");
            Leaderboard leaderboard = new Leaderboard(
                -1,
                (String)rankObject.get("name"),
                score.intValue()
            );
            leaderboards.add(leaderboard);
        }

        // 길드별 Rank 임의 세팅
        Collections.sort(leaderboards, Comparator.comparing(Leaderboard::getScore));
        for (int i = 0; i < leaderboards.size(); i++) {
            leaderboards.get(i).setRank(i + 1);
        }
        return leaderboards;
    }

    private String getGuildURL(String guildName) {
        return getCurrentKSTURL() + "/guilds/" + guildName;
    }

    public List<Leaderboard> parseGuildByName(String guildName)
    {
        String leaderboardURL = getGuildURL(guildName);
        String leaderboardData = null;
        try {
            leaderboardData = requestURL(leaderboardURL);
        } catch(Not200OK | IOException e) {
            e.printStackTrace();
            // alarm
            sendErrMsg("Guild (" + guildName + ") Request Error : " + e.getMessage());
            return null;
        }

        List<Leaderboard> leaderboards = null;
        try {
            leaderboards = guildJsonParser(leaderboardData);
        } catch (ParseException | NullPointerException | WrongJsonType e) {
            e.printStackTrace();
            sendErrMsg("Guild (" + guildName + ") Parse Error : " + e.getMessage());
            return null;
        }
        return leaderboards;
    }

}