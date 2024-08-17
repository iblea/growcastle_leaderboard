package parser.parser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

import java.util.LinkedList;

import parser.telegram.TelegramBot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import parser.entity.GuildMemberWave;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ParseGuild extends ParseAPI {

    private static Logger logger = LogManager.getLogger(ParseGuild.class);

    private String seasonName;
    private LocalDateTime parseTime;

    public ParseGuild(TelegramBot bot, String seasonName) {
        super(bot);
        this.seasonName = seasonName;
        this.parseTime = getCurrentTimeKST();
    }

    public ParseGuild(TelegramBot bot, String seasonName, LocalDateTime parseTime) {
        super(bot);
        this.seasonName = seasonName;
        this.parseTime = parseTime;
    }

    /**
     * 길드 내 길드원들의 이름(name), 웨이브(score) 를 파싱해
     * GuildMembers Entity 형식으로 리턴한다.
     *
     * @param guildName - 파싱할 Guild Name
     * @return List<GuildMembers>
     */
    public List<GuildMemberWave> parseGuildByName(String guildName)
    {
        String leaderboardURL = getGuildURL(guildName);
        String leaderboardData = null;
        try {
            leaderboardData = requestURL(leaderboardURL);
        } catch(Not200OK | IOException e) {
            logger.error("[{}] Request API Error", guildName);
            logger.error(e.getMessage());
            // alarm
            return Collections.<GuildMemberWave>emptyList();
        }

        List<GuildMemberWave> leaderboards = null;
        try {
            leaderboards = guildJsonParser(leaderboardData, guildName);
        } catch (ParseException | NullPointerException | WrongJsonType e) {
            logger.error("[{}] Parse Json Error", guildName);
            logger.error(e.getMessage());
            return Collections.<GuildMemberWave>emptyList();
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
    public List<GuildMemberWave> guildJsonParser(String leaderboardDataString, String guildName)
        throws ParseException, NullPointerException, WrongJsonType
    {
        JSONArray leaderboardlist = getAPIResponseData(leaderboardDataString);

        List<GuildMemberWave> leaderboards = new LinkedList<>();

        int minUnit = GuildMemberWave.getMinUnitAuto(this.parseTime);
        for (int i = 0; i < leaderboardlist.size(); i++) {
            JSONObject rankObject = (JSONObject)leaderboardlist.get(i);

            // Guild의 Player 정보에는 길드별 Rank에 대한 정보가 없다.
            Long score = (Long)rankObject.get("score");
            GuildMemberWave leaderboard = new GuildMemberWave(
                (String)rankObject.get("name"),
                guildName,
                score.intValue(),
                this.parseTime,
                this.seasonName,
                minUnit
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