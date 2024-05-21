from typing import Optional
import pytz
from datetime import datetime
import time
import requests
from requests import Response
import hashlib

import discord

from urllib3 import disable_warnings
from urllib3.exceptions import InsecureRequestWarning

disable_warnings(InsecureRequestWarning)


API_BASE_URL: str = "https://raongames.com/growcastle/restapi/season/"


def get_current_kst() -> datetime:
    kst_tz: any = pytz.timezone('Asia/Seoul')
    curr_time: datetime = datetime.now(kst_tz)
    return curr_time

def get_current_kst_date(curr_time: datetime) -> str:
    return "now"
    # return curr_time.strftime('%Y-%m-%d')

def get_parse_url(curr_time: datetime = get_current_kst()) -> str:
    return API_BASE_URL + get_current_kst_date(curr_time) + "/players"

def get_request_header():
    return {
        'User-Agent': "Mozilla/5.0"
    }


def request_api(url: str) -> Response:
    response: Response = requests.get(
        url=url,
        headers=get_request_header(),
        timeout=8,
        verify=False,
        allow_redirects=False
    )
    return response

def request_api_retry(url: str, retry_count: int, bot_alarm) -> Response:

    for i in range(retry_count):
        try:
            response: Response = requests.get(
                url=url,
                headers=get_request_header(),
                timeout=5,
                verify=False,
                allow_redirects=False
            )
            return response
        except Exception as e:
            errmsg: str = "retry {} count\nError: [{}]".format(i, str(e))
            bot_alarm.send(errmsg)


def get_context_to_json(response: Response) -> dict:
    try:
        jsondict: dict = response.json()
    except Exception:
        raise Exception("parse json error")

    return jsondict

def verify_api_json(apidict: dict) -> None:
    obj: any = apidict.get("code")
    if obj != 200:
        raise Exception ("api response json context code is not 200 [{}]".format(obj))

    obj = apidict.get("message")
    if obj != "ok":
        raise Exception ("api response json context message is not ok [{}]".format(obj))

    if "result" not in apidict:
        raise Exception ("api response json context no result object")

    result_obj: dict = apidict.get("result")
    if "date" not in result_obj:
        raise Exception ("api response json context no date object")

    if "list" not in result_obj:
        raise Exception ("api response json context no userlist object")

    date_obj: dict = result_obj.get("date")
    if "start" not in date_obj:
        raise Exception ("api response json context no date.start object")

    if "end" not in date_obj:
        raise Exception ("api response json context no date.end object")



def parse_response(response: Response) -> dict:
    if response.status_code != 200:
        errmsg: str = "response code is not 200, [{}]".format(response.status_code)
        raise Exception(errmsg)

    apidict : dict = get_context_to_json(response=response)
    verify_api_json(apidict)
    return apidict


def get_leaderboard_userlist(apidict: dict) -> list:
    leaderboard_userlist: list = apidict["result"]["list"]
    if len(leaderboard_userlist) == 0:
        raise Exception ("api response json context user list length is 0")
    return leaderboard_userlist


def get_season_last_date(apidict: dict) -> str:
    end_timestamp :str = apidict["result"]["date"]["end"]
    # yyyy-mm-ddThh:mm:ss
    end_date: str = end_timestamp.split("T")[0]
    return end_date



def parse_api(url) -> dict:

    try:
        response: Response = request_api(url)
    except Exception as e:
        print(e)

    apidata: dict = parse_response(response=response)

    return apidata

def get_monitoring_user(config) -> list:
    monitoring_user: list = [ x.get("name") for x in config["monitoring"]["player"] ]
    return monitoring_user

def get_monitoring_opt(config) -> dict:
    monitoring_opt: dict = {}

    for monitoring_object in config["monitoring"]["player"]:
        monitoring_opt[monitoring_object.get("name")] = {
            "rank_monitoring": monitoring_object.get("rank_monitoring"),
            "alert_user_id": monitoring_object.get("alert_user_id")
        }
    return monitoring_opt



def set_data_initialize(config: dict) -> dict:
    if "data" not in config:
        config["data"] = {}

    if "end_season" not in config["data"]:
        config["data"]["end_season"] = ""
    if "player_hash" not in config["data"]:
        config["data"]["player_hash"] = ""
    if "leaderboard" not in config["data"]:
        # leaderboard 에는 1, 2, 3, 4, 5, 6, 10, 11, 50, 51 위의 정보가 입력된다.
        config["data"]["leaderboard"] = {}
    if "users" not in config["data"]:
        config["data"]["users"] = {}

    return config


def set_leaderboard_data(cfg_data: dict, userlist: list) -> dict:

    # 순위는 51위까지만 저장한다.
    userlist_len: int = min(len(userlist), 51)

    rank_monitoring_list: list[int] = [1, 2, 3, 4, 5, 6, 10, 11, 50, 51]

    # 리스트를 순회하여 순위를 파싱하는 이유는
    # 동일한 웨이브일 경우, 순위가 중복되기 때문이다.
    # 예를들어 3위와 4위가 100 웨이브로 같을 경우,
    # rank에서는 두 플레이어 모두 다 3위로 표기한다.
    # 시즌 초에 동일한 웨이브로 인한 rank 값 세팅 오류가 많으므로 각별한 주의를 요한다.
    for i in range(userlist_len):
        userdata: dict = userlist[i]
        user_rank: int = i + 1
        rank_format = "r{}"
        if user_rank in rank_monitoring_list:
            cfg_data["leaderboard"][rank_format.format(user_rank)] = userdata.get("score")


    leaderboards: dict= cfg_data["leaderboard"]
    for i in rank_monitoring_list:
        if rank_format.format(i) in leaderboards:
            continue
        cfg_data["leaderboard"][rank_format.format(user_rank)] = -1

    return cfg_data


class ParsePlayer:
    bot: any = None

    config: Optional[dict] = None

    response: Optional[Response] = None
    apidict: Optional[dict] = None

    alert_list: list = []


    def __init__(self, bot, config):
        self.bot = bot
        self.config = config

    async def send_bot_msg(self, msg):
        if self.bot is None:
            return
        await self.bot.send(msg)

    def get_config(self):
        return self.config

    def get_alert_list(self):
        return self.alert_list

    def parse_leaderboard(self, curr_time, url=get_parse_url()) -> bool:
        """API를 파싱하여 crash 난 사람들의 리스트를 획득한다.
        """
        print("url : {}".format(url))
        self.alert_list = []
        self.response = None
        self.apidict = None
        retry_count: int = self.config.get("retry_count")
        for i in range(retry_count):
            try:
                self.response = request_api(url=url)
                self.apidict = parse_response(response=self.response)
            except Exception as e:
                self.apidict = None
                self.response = None
                errmsg: str = "retry {} count\nError: [{}]".format(i, str(e))
                print(errmsg)
                self.send_bot_msg(errmsg)

        if i >= retry_count:
            print("api request error")
            self.send_bot_msg("api request error")
            return False

        userlist: list  = get_leaderboard_userlist(apidict=self.apidict)
        end_season: str = get_season_last_date(apidict=self.apidict)

        self.config = set_data_initialize(config=self.config)
        cfg_data: dict  = self.config["data"]

        # player data 해시를 비교하여 api가 바뀌었는지 체크한다.
        sha256_hash: str = hashlib.sha256(string=self.response.text.encode()).hexdigest()
        if cfg_data["player_hash"] == sha256_hash:
            print("hash is same")
            return False

        cfg_data["player_hash"] = sha256_hash

        # set end_season date
        if end_season != cfg_data["end_season"]:
            cfg_data["end_season"] = end_season

        # config로부터 monitoring list 파싱
        player_monitoring: dict = self.config["monitoring"]["player"]

        # parse user and set alarm data
        cfg_data["leaderboard"] = {}
        cfg_user_data: dict = {}
        cfg_data = set_leaderboard_data(cfg_data=cfg_data, userlist=userlist)

        for i in range(len(userlist)):
            userdata: dict = userlist[i]
            username: str = userdata.get("name")
            if username not in player_monitoring:
                continue

            if username in cfg_data["users"]:
                self.wave_diff_and_set_alarm(username=username, curr_wave=userdata.get("score"))

            cfg_user_data[username] = {
                "score": userdata.get("score"),
                # "rank": userdata.get("rank"),
                "rank": i + 1 # rank 값이 중복될 수 있음을 확인했기에 내부적으로 for문을 돌려 rank 임의값을 구함.
            }

        cfg_data["users"] = cfg_user_data
        # self.wave_ranking_check()
        self.config["data"] = cfg_data
        self.config["data"]["last_parse_time"] = curr_time
        return True



    def wave_diff_and_set_alarm(self, username: str, curr_wave: int):

        check_crash_wave: int = self.config["crash_wave"]
        ago_wave: int = self.config["data"]["users"][username]["score"]
        player_monitoring: dict = self.config["monitoring"]["player"].get(username)

        if player_monitoring is None:
            return

        # crash
        # 시즌이 바뀌면 ago_wave가 curr_wave보다 클 수 있다.
        # 시즌이 바뀌는 시각에는 alarm을 잠시 중단한다.
        if curr_wave < ago_wave:
            return

        if ago_wave >= curr_wave - check_crash_wave:

            alert_user: str = player_monitoring.get("alert_user_id")
            self.alert_list.append({
                "ago_wave": ago_wave,
                "cur_wave": curr_wave,
                "user": alert_user,
                "username": username
            })

        else:
            self.config["monitoring"]["player"][username]["check"] = False



    def wave_ranking_check(self) -> None:
        # TODO: ranking 체크
        pass



    # response: Response = request_api_retry(url=url, retry_count=3, bot_alarm=bot)


def main():
    url = get_parse_url()
    print(url)

    response: Response = request_api(url=url)

    try:
        apidict: dict = parse_response(response)
        userlist: list  = get_leaderboard_userlist(apidict=apidict)
        end_date: str = get_season_last_date(apidict=apidict)
        print(userlist)
        print(end_date)
    except Exception as e:
        print("raise error")
        print(str(e))

def class_test():
    import config
    config_file_path: str = config.get_config_file_path()
    conf: dict= config.get_config_opt(config_file_path)
    if conf is None:
        print("error: cannot parse conf")
        return

    parser = ParsePlayer(bot=None, config=conf)

    start_time: float = time.time()
    parse_stat = parser.parse_leaderboard(curr_time=time.time())
    end_time: float = time.time()
    print(f"Elapsed time: {end_time - start_time:.3f} seconds")

    print("")
    print("")
    print(parser.get_config())

    print("parse_stat : {}".format(parse_stat))
    if parse_stat == True:
        config.set_config(config_dict=parser.get_config())
        print("")
        print(parser.get_alert_list())
    print("done")

if __name__ == "__main__":

    # main()
    class_test()
