package parser.parser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;



// import org.json.simple.parser.JSONParser;

import parser.telegram.TelegramBot;


public class ParseAPI {
    TelegramBot bot;
    private static Logger logger = LogManager.getLogger(ParseAPI.class);

    private static OkHttpClient okHttpClient = null;
    private static List<Protocol> protocols = null;


    private LocalDateTime startSeasonDate = null;
    private LocalDateTime endSeasonDate = null;
    private static final String APIBASEURL = "https://raongames.com/growcastle/restapi/season/";
    private static final int MAXTRY = 3;

    public ParseAPI(TelegramBot bot) {
        this.bot = bot;
        ParseAPI.create();
    }

    public static void create()
    {
        if (protocols == null) {
            logger.info("set OkHttp3 Protocols");
            setProtocols();
        }
        if (okHttpClient == null) {
            logger.info("set OkHttp3 Client");
            okHttpClient = createHttpClient(10, 30000);   // 10개의 연결, 30초 유지
        }
    }

    public static OkHttpClient getClient() {
        return okHttpClient;
    }

    public static List<Protocol> getProtocols() {
        return protocols;
    }

    private static void setProtocols() {
        if (protocols != null) {
            protocols = null;
        }
        protocols = new ArrayList<>();
        // protocols.add(Protocol.H2_PRIOR_KNOWLEDGE);
        protocols.add(Protocol.HTTP_2);
        protocols.add(Protocol.HTTP_1_1);
        logger.info("set OkHttp3 Protocols [HTTP/2, HTTP/1.1]");
    }

    private static OkHttpClient createHttpClient(int maxTotalConnections, long connectionKeepAliveTimeInMillis)
    {
        ConnectionPool connectionPool = new ConnectionPool(
            maxTotalConnections,
            connectionKeepAliveTimeInMillis,
            TimeUnit.MILLISECONDS
        );

        // SSLSocketFactory sslSocketFactory = getSslSocketFactory();
        // X509TrustManager trustManager = getDefaultTrustManager();

        return new OkHttpClient.Builder()
            .followRedirects(true)
            .protocols(protocols)
            .retryOnConnectionFailure(true)
            .connectionPool(connectionPool)
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            // .sslSocketFactory(sslSocketFactory, trustManager)
            .build();
    }



    public LocalDateTime getStartSeasonDate() {
        return startSeasonDate;
    }
    public LocalDateTime getEndSeasonDate() {
        return endSeasonDate;
    }

    public void setStartSeasonDateWithString(String startSeason) {
        // yyyy-mm-ddThh:mm:ss (UTC) -> KST
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(startSeason, formatter);
        // dateTime 에 1일 더하기
        dateTime = dateTime.plusDays(1).withHour(0).withMinute(0).withSecond(0);
        ZonedDateTime utcDateTime = dateTime.atZone(ZoneId.of("UTC"));
        // ZonedDateTime kstDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        // this.startSeasonDate = kstDateTime.toLocalDateTime();
        this.startSeasonDate = utcDateTime.toLocalDateTime();
    }

    public void setEndSeasonDateWithString(String endSeason) {
        // yyyy-mm-ddThh:mm:ss (UTC) -> KST
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(endSeason, formatter);
        dateTime = dateTime.withHour(23).withMinute(55).withSecond(0);
        ZonedDateTime utcDateTime = dateTime.atZone(ZoneId.of("UTC"));
        // ZonedDateTime kstDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        // this.endSeasonDate = kstDateTime.toLocalDateTime();
        this.endSeasonDate = utcDateTime.toLocalDateTime();
    }

    public LocalDateTime getCurrentTimeKST() {
        ZoneId kstZoneId = ZoneId.of("Asia/Seoul");
        return LocalDateTime.now(kstZoneId);
    }


    /**
     * kst 타임을 조합하여 요청할 URL을 리턴한다.
     * 해당 URL에 최종적으로 요청할 path를 조합한다.
     *
     * @return String
     */
    public String getCurrentURL() {
        // 특정 날짜에 대한 API 요청
        // return APIBASEURL + getCurrentKST();
        // 현재 시간에 대한 API 요청
        return APIBASEURL + getNow();
    }

    /**
     * API 요청 후 응답 헤더를 검증, payload를 리턴한다.
     * 응답 헤더는 200OK가 아닐 경우 에러를 리턴한다.
     *
     * @param urlString - 요청할 URL
     * @return String - 응답된 payload
     */
    public String requestUrlRetry(String urlString)
        throws IOException, Not200OK
    {
        String responseData = null;
        for (int try_count = 1; try_count <= MAXTRY; try_count++) {
            try {
                responseData = requestURL(urlString);
                if (responseData != null) {
                    break;
                }
            } catch(Not200OK | IOException e) {
                // logging
                if (try_count == MAXTRY) {
                    logger.error("requestURL failed");
                    logger.error(e.getMessage());
                    throw new Not200OK("requestURL failed : " + e.getMessage());
                }
            }
        }
        return responseData;
    }

    public String requestURL(String urlString)
        throws IOException, Not200OK
    {

        Request request = new Request.Builder()
            .url(urlString)
            .header("User-Agent", "Mozilla/5.0")
            .build();

        OkHttpClient okClient = ParseAPI.getClient();
        Response response = okClient.newCall(request).execute();
        if (response.code() != 200) {
            String errMsg = "HTTP Response Code is not 200 OK [" + response.code() + "]";
            logger.error(errMsg);
            throw new Not200OK(errMsg);
        }
        return response.body().string();
    }

    /**
     * 응답 payload에서 json을 파싱하여 원하는 정보만 추출해 리턴한다.
     *
     * @param responseData - 응답 payload
     * @return JSONArray - 리더보드/유저 리스트
     */
    public JSONArray getAPIResponseData(String responseData)
        throws ParseException, NullPointerException, WrongJsonType
    {
        if (responseData == null) {
            logger.error("responseData is NULL");
            throw new NullPointerException("responseData is NULL");
        }
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject)jsonParser.parse(responseData);
        validateAPIData(jsonObject);

        JSONObject results = (JSONObject)jsonObject.get("result");
        if (results == null) {
            logger.error("cannot find result object");
            throw new NullPointerException("cannot find result object");
        }

        // endseason date 정보를 저장한다.
        JSONObject seasonDate = (JSONObject)results.get("date");
        // yyyy-mm-ddThh:mm:ss (UTC)
        String startSeason = (String)seasonDate.get("start");
        String endSeason = (String)seasonDate.get("end");

        setStartSeasonDateWithString(startSeason);
        setEndSeasonDateWithString(endSeason);

        JSONArray apiDataList = (JSONArray)results.get("list");
        if (apiDataList == null) {
            logger.error("cannot find list array");
            throw new NullPointerException("cannot find list array");
        }
        if (apiDataList.isEmpty()) {
            logger.error("no playerList Array data");
            throw new WrongJsonType("no playerList Array data");
        }
        return apiDataList;
    }




    public String getNow() {
        return "now";
    }

    /**
     * API URL에 요청할 KST 타임을 획득한다. (api url은 kst 타임을 기준으로 요청해야 한다.)
     *
     * @return String (yyyy-mm-dd)
     */
    public String getCurrentKST() {
        // 현재 시간을 KST 기준으로 가져오기
        ZonedDateTime currentTimeKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        // "yyyy-MM-dd" 형식으로 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return currentTimeKST.format(formatter);
    }


    /**
     * Json의 정상 유무를 검증한다.
     * 비정상 시 에러를 생성한다.
     * { "code": 200, "message": "ok" ... } 는
     * growcastle API의 요청에 대한 정상 응답 시 항상 포함되는 구문이다.
     *
     * @param apiData - payload로부터 파싱된 jsonObject
     */
    public void validateAPIData(JSONObject apiData)
        throws WrongJsonType
    {
        Long codeObj = (Long)apiData.get("code");
        String messageObj = (String)apiData.get("message");

        if (codeObj == null) {
            throw new NullPointerException("cannot find code object");
        }
        if (messageObj == null) {
            throw new NullPointerException("cannot find message object");
        }

        if (codeObj != 200) {
            throw new WrongJsonType("json code is not " + codeObj.toString());
        }
        if (!messageObj.equals("ok")) {
            throw new WrongJsonType("json message is " + messageObj);
        }
    }

    protected void sendErrMsg(String errMsg) {
        logger.error(errMsg);
        // System.out.println(errMsg);
    }

}