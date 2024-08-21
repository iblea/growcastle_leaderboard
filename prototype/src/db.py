from typing import Optional
import pytz
from datetime import datetime
from datetime import timedelta
import time
import hashlib
from urllib import parse
import copy

import discord
import psycopg

import re

# 정규식 패턴 정의
username_pattern = r'^[a-zA-Z0-9 _-]+$'
compiled_username_pattern = re.compile(username_pattern)

def open_conn(config):
    conn = None
    try:
        postgresql_url = "postgresql://{}:{}@{}:{}/{}".format(
            config["db"]["username"],
            parse.quote(config["db"]["password"]),
            config["db"]["host"],
            config["db"]["port"],
            config["db"]["database"]
        )
        conn = psycopg.connect(postgresql_url)
        # print("db conn open")
    except Exception as e:
        print("================= open conn error =================")
        print(e)
        conn = None

    return conn

def conn_close(conn) -> None:
    if conn is None:
        return None

    try:
        conn.close()
        # print("db conn close")
    except Exception as e:
        print("================= conn close error =================")
        print(e)
    conn = None
    return None

def get_cursor(conn):
    if conn is None:
        return None

    cur = None
    try:
        cur = conn.cursor()
    except Exception as e:
        print("================= get cursor error =================")
        print(e)
        cur = None
    return cur





def get_leaderboards(cur):
    if cur is None:
        return None

    leaderboards = None
    try:
        cur.execute("""SELECT rank, name, score, parsetime
            FROM leaderboard_player order by rank asc""")
        leaderboards = cur.fetchall()
    except Exception as e:
        print("================= get leaderboard error =================")
        print(e)
        leaderboards = None

    return leaderboards


def get_historydata_by_username(cur, username):

    sql_format= """SELECT rank, score, parsetime
        FROM leaderboard_player WHERE name = '{}' and min_unit = 0 order by parsetime desc limit 3"""

    history_data = None
    try:
        cur.execute(sql_format.format(username))
        history_data = cur.fetchall()
    except Exception as e:
        print("================= get historydata error =================")
        print(e)
        history_data = None

    if history_data is None:
        return None

    if len(history_data) == 0:
        return None

    history_dict = {}
    history_dict["last_update_time"] = history_data[0][2].timestamp()
    for i in range(len(history_data)):
        rank = history_data[i][0]
        score = history_data[i][1]
        parsetime = history_data[i][2].strftime("%Y-%m-%d %H:%M:%S")
        history_dict[parsetime] = {
            "rank": rank,
            "score": score
        }

    return history_dict


def get_seasons(cur):
    if cur is None:
        return None

    seasons = None
    try:
        cur.execute("SELECT start_date, end_date FROM seasondata")
        seasons = cur.fetchall()
    except Exception as e:
        print("================= get endseason error =================")
        print(e)
        seasons = None

    if seasons is None:
        return None

    if len(seasons) != 1:
        print("error: endseason data is not unique")
        return None

    if seasons[0][0] is None or seasons[0][0] == "":
        print("error: cannot get startseason")
        return None
    if seasons[0][1] is None or seasons[0][1] == "":
        print("error: cannot get endseason")
        return None

    season_date = {
        "start": seasons[0][0],
        "end": seasons[0][1]
    }

    return season_date


def get_historys(cur, username):
    if cur is None:
        return None
    if username is None:
        return None
    if username == "":
        return None

    sql = """WITH ranked_data AS (
    SELECT
        name,
        rank,
        score,
        parsetime,
        wave,
        hornjump,
        dhornjump,
        crystaljump,
        CASE
            WHEN EXTRACT(MINUTE FROM parsetime) > 0 THEN DATE_TRUNC('hour', parsetime) + INTERVAL '1 hour'
            ELSE DATE_TRUNC('hour', parsetime)
        END AS parsetime_1h
    FROM
        history_player
)
SELECT
    name,
    parsetime_1h,
	COALESCE(MAX(rank) FILTER (WHERE EXTRACT(MINUTE FROM parsetime) = 0), -1) AS rank,
    MAX(score) AS score,
    SUM(wave) AS total_wave,
    SUM(hornjump) AS total_hornjump,
    SUM(dhornjump) AS total_dhornjump,
    SUM(crystaljump) AS total_crystaljump
FROM
    ranked_data
WHERE
	LOWER(name) = LOWER('{}')
GROUP BY
    name, parsetime_1h
ORDER BY
    name, parsetime_1h;
"""

    userData = None
    try:
        cur.execute(sql.format(username))
        userData = cur.fetchall()
    except Exception as e:
        print("================= get historys error =================")
        print(e)
        userData = None

    return userData

    # histories = []
    # for hourData in userData:
    #     data = {
    #         "name": hourData[0],
    #         "parse_time": hourData[1],
    #         "rank": hourData[2],
    #         "score": hourData[3],
    #         "wave": hourData[4],
    #         "hornjump": hourData[5],
    #         "dhornjump": hourData[6],
    #         "crystaljump": hourData[7]
    #     }
    #     histories.append(data)
    # return histories




def set_leaderboard_data(leaderboards) -> dict:

    userlist_len = min(len(leaderboards), 51)

    rank_monitoring_list: list[int] = [1, 2, 3, 4, 5, 6, 10, 11, 50, 51]
    leaderboards_dict = {}
    rank_format = "r{}"

    for init_rank in rank_monitoring_list:
        leaderboards_dict[rank_format.format(init_rank)] = -1

    for i in range(userlist_len):
        userdata = leaderboards[i]
        rank = userdata[0]
        score = userdata[2]
        if rank in rank_monitoring_list:
            leaderboards_dict[rank_format.format(rank)] = score
    return leaderboards_dict



class ParsePlayer:
    bot: any = None

    config: Optional[dict] = None

    apidict: Optional[dict] = None

    alert_list: dict = {}
    conn = None


    def __init__(self, bot, config):
        self.bot = bot
        self.config = config
        self.conn = None

    async def send_bot_msg(self, msg):
        if self.bot is None:
            return
        await self.bot.send(msg)

    def getConn(self):
        return self.conn

    def get_config(self):
        return self.config

    def get_alert_list(self):
        return self.alert_list


    async def parse_leaderboard(self, curr_time) -> bool:

        if self.config["db"]["port"] == 0:
            return False

        if self.conn is None:
            self.conn = open_conn(self.config)
            if self.conn is None:
                return False

        cur = get_cursor(self.conn)
        if cur is None:
            print("error: cannot get cursor")
            self.conn = conn_close(self.conn)
            return False

        seasonData = get_seasons(cur)
        if seasonData is None:
            print("error: cannot get seasondata")
            return False

        leaderboards = get_leaderboards(cur)
        if leaderboards is None:
            print("data is None or sql error")
            return False
        if len(leaderboards) == 0:
            print("data is empty")
            return False

        cfg_data: dict  = self.config["data"]
        db_parseTime: str = leaderboards[0][3].timestamp()

        if "player_hash" in cfg_data:
            # db 내용이 업데이트 되지 않는 것
            if db_parseTime == cfg_data["player_hash"]:
                return False

        cfg_data["player_hash"] = db_parseTime
        cfg_data["seasons"] = {}
        cfg_data["seasons"]["start"] = seasonData["start"].strftime("%Y-%m-%d %H:%M:%S")
        cfg_data["seasons"]["end"] = seasonData["end"].strftime("%Y-%m-%d %H:%M:%S")
        cfg_data["leaderboard"] = set_leaderboard_data(leaderboards)

        # config로부터 monitoring list 파싱
        player_monitoring: dict = self.config["monitoring"]["player"]

        cfg_user_data: dict = {}
        for i in range(len(leaderboards)):
            userdata: dict = leaderboards[i]
            rank = userdata[0]
            username: str = userdata[1]
            score = userdata[2]
            parseTime = int(userdata[3].timestamp())
            if username not in player_monitoring:
                continue

            if "users" in cfg_data and username in cfg_data["users"]:
                ago_score = cfg_data["users"][username]["score"]
                if ago_score == score:
                    cfg_user_data[username] = {
                        "score": score,
                        "rank": rank,
                        "last_wave_time": cfg_data["users"][username]["last_wave_time"],
                    }
                else:
                    cfg_user_data[username] = {
                        "score": score,
                        "rank": rank,
                        "last_wave_time": parseTime,
                    }

                self.wave_diff_and_set_alarm(username=username, user_data=cfg_user_data[username])
            else:
                cfg_user_data[username] = {
                    "score": score,
                    "rank": rank,
                    "last_wave_time": parseTime,
                }

        cfg_data["users"] = cfg_user_data
        # self.wave_ranking_check()
        self.config["data"] = cfg_data
        self.config["data"]["last_parse_time"] = curr_time

        return True

    def wave_diff_and_set_alarm(self, username: str, user_data: dict):

        # TODO: crash time config로부터 가져오기
        crash_time = 200
        player_monitoring: dict = self.config["monitoring"]["player"].get(username)

        last_wave_time = user_data["last_wave_time"]

        if player_monitoring is None:
            return

        if last_wave_time + crash_time < int(time.time()):
            alert_user: str = player_monitoring.get("alert_user_id")
            self.alert_list[username] = {
                "user": alert_user,
                "username": username,
                "last_wave_time": last_wave_time
            }
        else:
            self.config["monitoring"]["player"][username]["check"] = False
            if username in self.alert_list:
                del self.alert_list[username]

    def get_history(self, username: str):
        if self.config["db"]["port"] == 0:
            return None

        history_data = None
        for _ in range(5):
            if self.conn is None:
                self.conn = open_conn(self.config)
                if self.conn is None:
                    continue

            cur = get_cursor(self.conn)
            if cur is None:
                print("error: cannot get cursor")
                self.conn = conn_close(self.conn)
                continue

            if not ("seasons" in self.config["data"]
                    and "start" in self.config["data"]["seasons"]
                    and "end" in self.config["data"]["seasons"]
                ):
                self.config["data"]["seasons"] = {}
                seasonData = get_seasons(cur)
                if seasonData is None:
                    print("error: cannot get seasondata")
                    continue
                self.config["data"]["seasons"]["start"] = seasonData["start"].strftime("%Y-%m-%d %H:%M:%S")
                self.config["data"]["seasons"]["end"] = seasonData["end"].strftime("%Y-%m-%d %H:%M:%S")

            end_season = datetime.strptime(self.config["data"]["seasons"]["end"], "%Y-%m-%d %H:%M:%S")
            modify_end_season = end_season.replace(hour=23, minute=58, second=0, microsecond=0)
            if modify_end_season <= datetime.now():
                self.config["data"]["seasons"] = {}
                seasonData = get_seasons(cur)
                if seasonData is None:
                    print("error: cannot get seasondata")
                    continue
                self.config["data"]["seasons"]["start"] = seasonData["start"].strftime("%Y-%m-%d %H:%M:%S")
                self.config["data"]["seasons"]["end"] = seasonData["end"].strftime("%Y-%m-%d %H:%M:%S")

            # cfg_data: dict  = self.config["data"]

            history_data = get_historys(cur, username)
            # none 결과값은 db 에러난 것임. 데이터가 없으면 빈 리스트 반환
            if history_data is None:
                print("error: data is None or sql error")
                continue

            if len(history_data) == 0:
                return []

            start_season = datetime.strptime(self.config["data"]["seasons"]["start"], "%Y-%m-%d %H:%M:%S")
            list_season = []
            season_rotate = copy.deepcopy(start_season)

            curtime = datetime.now()
            if curtime.minute == 0 and curtime.second == 0:
                curtime = curtime.replace(minute=0, second=1)
            else:
                curtime = (curtime + timedelta(hours=1)).replace(minute=0, second=1)
            while season_rotate < curtime:
                list_season.append(season_rotate)
                season_rotate = season_rotate + timedelta(hours=1)

            userData = []

            ago_wave = 0
            for list_season_time in list_season:
                found = False
                diff = list_season_time - start_season
                # parse_time_string = "{} day {:2d} hour".format(diff.days, diff.seconds // 3600)
                parse_time_string = "{:2d}".format(diff.seconds // 3600)
                for i in range(len(history_data)):
                    if history_data[i][1] == list_season_time:
                        data = history_data[i]
                        diff = data[3] - ago_wave
                        if diff >= 10000 or diff < 0:
                            diff = -1
                        userData.append({
                            "name": data[0],
                            "parse_time": parse_time_string,
                            "rank": data[2],
                            "score": data[3],
                            "wave": data[4],
                            "diff": diff,
                            "hornjump": data[5],
                            "dhornjump": data[6],
                            "crystaljump": data[7]
                        })
                        ago_wave = data[3]
                        found = True
                        history_data.pop(i)
                        break
                if found is False:
                    userData.append({
                        "name": "",
                        "parse_time": parse_time_string,
                        "rank": 0,
                        "score": 0,
                        "wave": 0,
                        "diff": 0,
                        "hornjump": 0,
                        "dhornjump": 0,
                        "crystaljump": 0
                    })
                    ago_wave = 0

            break
        return userData


def get_history_chart(data) -> str:
    if len(data) == 0:
        return "```\nno data\n```\n"

    string = "```\n"
    string += "|------+-----+--------+------+-----+-----+-----+-----|\n"
    string += "| time | rnk | score  | diff | per | hrn | dhn | cjp |\n"
    string += "|------+-----+--------+------+-----+-----+-----+-----|\n"
    for item in data:
        string += "| {} h | {:3d} | {:6d} | {:4d} | {:3d} | {:3d} | {:3d} | {:3d} |\n".format(
            item["parse_time"],
            item["rank"],
            item["score"],
            item["diff"],
            item["wave"],
            item["hornjump"],
            item["dhornjump"],
            item["crystaljump"]
        )
    string += "|------+-----+--------+------+-----+-----+-----+-----|\n"
    string += "```\n"

    return string


def get_history_string(data) -> str:
    if len(data) == 0:
        return "```\nno data\n```\n"

    string = "```\n"
    # 10|aaa,bbbbbb,cccc|ddd,ooo,pp,qqq
    for item in data:
        string += "{}|{:3d},{:6d},{:4d}|{:3d},{:3d},{:2d},{:3d}\n".format(
            item["parse_time"],
            item["rank"],
            item["score"],
            item["diff"],
            item["wave"],
            item["hornjump"],
            item["dhornjump"],
            item["crystaljump"]
        )
    string += "```\n"

    return string



# 정규식 검사
# username은 대소문자/숫자/공백/-/_ 만 허용
def arg_check(username: str):
    global compiled_username_pattern
    # 정규식 검사
    if compiled_username_pattern.match(username):
        return True
    else:
        return False




def test2():
    import config
    print("test main")
    config_file_path: str = config.get_config_file_path()
    conf: dict= config.get_config_opt(config_file_path)
    if conf is None:
        print("error: cannot parse conf")

    db_parser = ParsePlayer(None, conf)

    name="ib"
    result = db_parser.get_history(name)
    if result is None:
        print("error: cannot get history")
        return

    print("")
    print("")
    string = "'{}' user data\n\n".format(name)
    for i in range(5):
        string += "{} day data\n".format(i + 1)
        string += get_history_chart(result[24*i:24*(i+1)])
        print(string)
        print("")
        string = ""
    print("")

def test():
    import config
    print("test main")
    config_file_path: str = config.get_config_file_path()
    conf: dict= config.get_config_opt(config_file_path)
    if conf is None:
        print("error: cannot parse conf")
        return 1

    conn = open_conn(conf)
    if conn is None:
        print("error: cannot connect db")
        return 1

    print("connect success")

    print("first connect")

    leaderboards = None
    cur = get_cursor(conn)
    if cur is None:
        print("error: cannot get cursor")
        conn = conn_close(conn)
    else:
        print(get_seasons(cur))
        leaderboards = get_leaderboards(cur)
        if leaderboards is None:
            print("data is None or sql error")
        else:
            print(leaderboards[0])
            print("unixtime: ", int(leaderboards[0][3].timestamp()))

    return 0


if __name__ == "__main__":
    test2()
