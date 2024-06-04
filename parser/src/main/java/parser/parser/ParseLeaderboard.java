package parser.parser;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import parser.telegram.TelegramBot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import parser.entity.Leaderboard;


/**
 * 리더보드 정보를 파싱하는 클래스
 * player (플레이어), guild (길드), hell (무한웨이브) 리더보드에 대한 응답값을 파싱한다.
 * 어느 타입이든, 리더보드 타입은 모두 rank, score, name 로 구성되어 있다.
 */
public class ParseLeaderboard extends ParseAPI {

    public ParseLeaderboard(TelegramBot bot) {
        super(bot);
    }

    /**
     * 리더보드 정보를 가져와 LeaderBoard Entity 형식으로 리턴한다.
     *
     * @param leaderboardType
     * @return
     */
    public List<Leaderboard> parseLeaderboards(String leaderboardType)
    {
        String leaderboardURL = getLeaderboardURL(leaderboardType);
        String leaderboardData = null;
        try {
            leaderboardData = requestURL(leaderboardURL);
        } catch(Not200OK | IOException e) {
            e.printStackTrace();
            // alarm
            sendErrMsg("Leaderboard (" + leaderboardType + ") Request Error : " + e.getMessage());
            return null;
        }

        List<Leaderboard> leaderboards = null;
        try {
            leaderboards = leaderboardJsonParser(leaderboardData);
        } catch (ParseException | NullPointerException | WrongJsonType e) {
            e.printStackTrace();
            sendErrMsg("Leaderboard (" + leaderboardType + ") Parse Error : " + e.getMessage());
            return null;
        }
        return leaderboards;
    }

    /**
     * 요청할 API URL을 만든다.
     * @param leaderboardType - 요청할 URL 타입 ( players / guilds )
     * @return String
     */
    private String getLeaderboardURL(String leaderboardType) {
        return getCurrentURL() + "/" + leaderboardType;
    }

    /**
     * 리턴된 Json을 파싱한다.
     *
     * @param leaderboardDataString
     * @return List<Leaderboard>
     */
    public List<Leaderboard> leaderboardJsonParser(String leaderboardDataString)
        throws ParseException, NullPointerException, WrongJsonType
    {
        JSONArray leaderboardlist = getAPIResponseData(leaderboardDataString);

        List<Leaderboard> leaderboards = new ArrayList<Leaderboard>();
        for (int i = 0; i < leaderboardlist.size(); i++) {
            JSONObject rankObject = (JSONObject)leaderboardlist.get(i);
            leaderboards.add(getLeaderboardInJson(rankObject, i));
        }
        return leaderboards;
    }

    /**
     * 리더보드 Json 엔티티 형식을 Leaderboard JDO 로 변환한다.
     * 랭크는 불가피하게 for문을 순회해가며 순위를 매긴다.
     * 동일한 웨이브의 경우 이를 동일한 순위로 부여한다.
     * ex: A유저 100wave, B유저 80wave, C유저 80wave 일 때 B,C 유저의 랭크를 모두 2로 부여한다.
     * 이 경우 특정 시간에 해당 랭크의 정보가 없을 수 있으므로, 순위 계산에 오류가 생길 수 있다.
     * 따라서 for문을 순회하며 순위를 매겨 rank를 부여해 동일한 시간에 중복되는 랭크가 없도록 한다.
     *
     * @param rankObject - rank json object
     * @param rank - 리더보드 순위
     * @return Leaderboard - 리더보드 JDO Entity
     */
    Leaderboard getLeaderboardInJson(JSONObject rankObject, int rank) {
        // Integer rank = (Integer)rankObject.get("rank");
        Integer score = (Integer)rankObject.get("score");
        return new Leaderboard(
            rank + 1,
            (String)rankObject.get("name"),
            score.intValue()
        );
    }

}