package parser.parser;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

public class ParseAPITest {

    @Test
    void DateTypeTest() {
        ParseAPI url = new ParseAPI(null);
        String getURL = url.getCurrentKST();
        assertThat(Pattern.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}$", getURL)).isEqualTo(true);
    }

    // @Test
    // void PlayerRequestTest() {
    //     ParsePlayer parse = new ParsePlayer(null);
    //     parse.parsePlayer();
    // }


    String jsonNormalData = """
{
  "code": 200,
  "message": "ok",
  "result": {
    "date": {
      "start": "2024-04-29T15:00:00",
      "end": "2024-05-04T14:59:59"
    },
    "list": [
      {
        "rank": 1,
        "name": "SY_Lotuszz",
        "score": 151231
      },
      {
        "rank": 2,
        "name": "GR_MadGlenz",
        "score": 133219
      },
      {
        "rank": 3,
        "name": "yue_er",
        "score": 130000
      },
      {
        "rank": 4,
        "name": "El444",
        "score": 127025
      },
      {
        "rank": 5,
        "name": "100ADS PER DAY",
        "score": 125535
      },
      {
        "rank": 6,
        "name": "Ib",
        "score": 118247
      },
      {
        "rank": 7,
        "name": "MrMedved",
        "score": 115557
      },
      {
        "rank": 8,
        "name": "UD_V_T_K",
        "score": 115061
      },
      {
        "rank": 9,
        "name": "UD_Alonso1",
        "score": 114042
      },
      {
        "rank": 10,
        "name": "UD_Red",
        "score": 113560
      }
    ]
  }
}
""";

    @Test
    void responseAPIJsonParse()
        throws ParseException, NullPointerException, WrongJsonType
    {
        ParseAPI parse = new ParseAPI(null);
        JSONArray playerlist = parse.getAPIResponseData(jsonNormalData);
        assertThat(playerlist.size()).isEqualTo(10);
    }
}