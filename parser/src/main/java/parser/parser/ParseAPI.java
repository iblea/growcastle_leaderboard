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

    public String getCurrentKST() {
        // 현재 시간을 KST 기준으로 가져오기
        ZonedDateTime currentTimeKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        // "yyyy-MM-dd" 형식으로 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return currentTimeKST.format(formatter);
    }

    public String getCurrentKSTURL() {
        return APIBASEURL + getCurrentKST();
    }

    public HttpURLConnection setConnection(URL url)
        throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setDoOutput(true);
        return conn;
    }

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

    protected void sendErrMsg(String errMsg) {
        System.out.println(errMsg);
    }

}