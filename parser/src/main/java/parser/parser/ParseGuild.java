package parser.parser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import parser.telegram.TelegramBot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import parser.entity.GuildMember;


public class ParseGuild extends ParseAPI {

    LocalDateTime parseTime;

    public ParseGuild(TelegramBot bot) {
        super(bot);
        this.parseTime = getCurrentTimeKST();
    }

    public ParseGuild(TelegramBot bot, LocalDateTime parseTime) {
        super(bot);
        this.parseTime = parseTime;
    }

    /**
     * 길드 내 길드원들의 이름(name), 웨이브(score) 를 파싱해
     * GuildMembers Entity 형식으로 리턴한다.
     *
     * @param guildName - 파싱할 Guild Name
     * @return List<GuildMembers>
     */
    public List<GuildMember> parseGuildByName(String guildName)
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

        List<GuildMember> leaderboards = null;
        try {
            leaderboards = guildJsonParser(leaderboardData);
        } catch (ParseException | NullPointerException | WrongJsonType e) {
            e.printStackTrace();
            sendErrMsg("Guild (" + guildName + ") Parse Error : " + e.getMessage());
            return null;
        }
        return leaderboards;
    }

    /**
     * 리턴된 Json을 파싱한다.
     * Guild별 길드원 조회 결과에는 rank 값이 없기 때문에
     * score 별로 정렬해 rank 값을 직접 세팅한다.
     *
     * @param leaderboardDataString - API 응답받은 Json
     * @return List<GuildMembers>
     */
    public List<GuildMember> guildJsonParser(String leaderboardDataString)
        throws ParseException, NullPointerException, WrongJsonType
    {
        JSONArray leaderboardlist = getAPIResponseData(leaderboardDataString);

        List<GuildMember> leaderboards = new ArrayList<>();
        for (int i = 0; i < leaderboardlist.size(); i++) {
            JSONObject rankObject = (JSONObject)leaderboardlist.get(i);

            // Guild의 Player 정보에는 길드별 Rank에 대한 정보가 없다.
            Long score = (Long)rankObject.get("score");
            GuildMember leaderboard = new GuildMember(
                (String)rankObject.get("name"),
                score.intValue(),
                this.parseTime
            );
            leaderboards.add(leaderboard);
        }

        return leaderboards;
    }

    /**
     * 요청할 API URL을 만든다.
     * @param guildName - 검색할 길드명
     * @return String
     */
    private String getGuildURL(String guildName) {
        return getCurrentURL() + "/guilds/" + guildName;
    }

}