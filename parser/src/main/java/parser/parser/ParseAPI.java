package parser.parser;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// import org.json.simple.parser.JSONParser;

import parser.telegram.TelegramBot;


public class ParseAPI {
    TelegramBot bot;
    final private String APIBASEURL = "https://raongames.com/growcastle/restapi/season/";

    public ParseAPI(TelegramBot bot) {
        this.bot = bot;
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
    public String requestURL(String urlString)
        throws IOException, Not200OK
    {
        HttpURLConnection conn = setConnection(new URL(urlString));
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String errMsg = "HTTP Response Code is not 200 OK [" + String.valueOf(responseCode) + "]";
            throw new Not200OK(errMsg);
        }
        return parseResponse(conn.getInputStream());
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
            throw new NullPointerException("responseData is NULL");
        }
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject)jsonParser.parse(responseData);
        validateAPIData(jsonObject);

        JSONObject results = (JSONObject)jsonObject.get("result");
        if (results == null) {
            throw new NullPointerException("cannot find result object");
        }
        JSONArray apiDataList = (JSONArray)results.get("list");
        if (apiDataList == null) {
            throw new NullPointerException("cannot find list array");
        }
        if (apiDataList.size() == 0) {
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
     * API에 요청할 헤더, 메서드를 세탕한다.
     *
     * @param url - 요청할 URL
     * @return HttpURLConnection - 요청할 객체
     */
    public HttpURLConnection setConnection(URL url)
        throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setDoOutput(true);
        return conn;
    }

    /**
     * API 요청 후 응답받은 payload를 String 형태로 변환한다.
     *
     * @param inputStream - response 된 payload inputStream
     * @return String
     */
    public String parseResponse(InputStream inputStream)
        throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer stringBuffer = new StringBuffer();
        String inputLine;

        while ((inputLine = bufferedReader.readLine()) != null)  {
            stringBuffer.append(inputLine);
        }
        String responseData = stringBuffer.toString();
        bufferedReader.close();
        return responseData;
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
        System.out.println(errMsg);
    }

}