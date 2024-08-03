package parser.parser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import parser.telegram.TelegramBot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import parser.entity.LeaderboardBaseEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 리더보드 정보를 파싱하는 클래스
 * player (플레이어), guild (길드), hell (무한웨이브) 리더보드에 대한 응답값을 파싱한다.
 * 어느 타입이든, 리더보드 타입은 모두 rank, score, name 로 구성되어 있다.
 */
public class ParseLeaderboard extends ParseAPI {

    private static Logger logger = LogManager.getLogger(ParseLeaderboard.class);

    String leaderboardType = "";
    LocalDateTime parseTime;

    private ParseLeaderboard(TelegramBot bot, String type) {
        super(bot);
        this.leaderboardType = type;
        this.parseTime = getCurrentTimeKST();
    }

    private ParseLeaderboard(TelegramBot bot, String type, LocalDateTime parseTime) {
        super(bot);
        this.leaderboardType = type;
        this.parseTime = parseTime;
    }


    public static ParseLeaderboard player(TelegramBot bot) {
        return new ParseLeaderboard(bot, "players");
    }

    public static ParseLeaderboard player(TelegramBot bot, LocalDateTime parseTime) {
        return new ParseLeaderboard(bot, "players", parseTime);
    }

    public static ParseLeaderboard guild(TelegramBot bot) {
        return new ParseLeaderboard(bot, "guilds");
    }
    public static ParseLeaderboard guild(TelegramBot bot, LocalDateTime parseTime) {
        return new ParseLeaderboard(bot, "guilds", parseTime);
    }

    public static ParseLeaderboard hellmode(TelegramBot bot) {
        return new ParseLeaderboard(bot, "hell");
    }
    public static ParseLeaderboard hellmode(TelegramBot bot, LocalDateTime parseTime) {
        return new ParseLeaderboard(bot, "hell", parseTime);
    }


    /**
     * 리더보드 정보를 가져와 LeaderBoard Entity 형식으로 리턴한다.
     *
     * @param leaderboardType
     * @return
     */
    public List<LeaderboardBaseEntity> parseLeaderboards()
    {
        String leaderboardURL = getLeaderboardURL();
        String leaderboardData = null;
        try {
            leaderboardData = requestURL(leaderboardURL);
        } catch(Not200OK | IOException e) {
            logger.error("Leaderboard Request API Error");
            logger.error(e.getMessage());
            // alarm
            sendErrMsg("Leaderboard (" + this.leaderboardType + ") Request Error : " + e.getMessage());
            return null;
        }

        List<LeaderboardBaseEntity> leaderboards = null;
        try {
            leaderboards = leaderboardJsonParser(leaderboardData);
        } catch (ParseException | NullPointerException | WrongJsonType e) {
            logger.error("Leaderboard Parse Json Error");
            logger.error(e.getMessage());
            sendErrMsg("Leaderboard (" + this.leaderboardType + ") Parse Error : " + e.getMessage());
            return null;
        }
        return leaderboards;
    }

    /**
     * 리턴된 Json을 파싱한다.
     *
     * @param leaderboardDataString
     * @return List<LeaderboardBaseEntity>
     */
    public List<LeaderboardBaseEntity> leaderboardJsonParser(String leaderboardDataString)
        throws ParseException, NullPointerException, WrongJsonType
    {
        JSONArray leaderboardlist = getAPIResponseData(leaderboardDataString);

        List<LeaderboardBaseEntity> leaderboards = new ArrayList<>();
        for (int i = 0; i < leaderboardlist.size(); i++) {
            JSONObject rankObject = (JSONObject)leaderboardlist.get(i);
            leaderboards.add(getLeaderboardInJson(rankObject, i));
        }
        return leaderboards;
    }

    /**
     * 요청할 API URL을 만든다.
     * @param leaderboardType - 요청할 URL 타입 ( players / guilds )
     * @return String
     */
    private String getLeaderboardURL() {
        return getCurrentURL() + "/" + this.leaderboardType;
    }

    /**
     * 리더보드 Json 엔티티 형식을 LeaderboardBaseEntity JDO 로 변환한다.
     * 랭크는 불가피하게 for문을 순회해가며 순위를 매긴다.
     * 동일한 웨이브의 경우 이를 동일한 순위로 부여한다.
     * ex: A유저 100wave, B유저 80wave, C유저 80wave 일 때 B,C 유저의 랭크를 모두 2로 부여한다.
     * 이 경우 특정 시간에 해당 랭크의 정보가 없을 수 있으므로, 순위 계산에 오류가 생길 수 있다.
     * 따라서 for문을 순회하며 순위를 매겨 rank를 부여해 동일한 시간에 중복되는 랭크가 없도록 한다.
     *
     * @param rankObject - rank json object
     * @param rank - 리더보드 순위
     * @return LeaderboardBaseEntity - 리더보드 JDO Entity
     */
    private LeaderboardBaseEntity getLeaderboardInJson(JSONObject rankObject, int rank) {
        // Integer rank = (Integer)rankObject.get("rank");
        // Integer 시 Class Cast Exception 발생
        Long score = (Long)rankObject.get("score");
        return new LeaderboardBaseEntity(
            rank + 1,
            (String)rankObject.get("name"),
            score.intValue(),
            this.parseTime
        );
    }

}