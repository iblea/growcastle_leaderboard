
import os
import json


def get_script_path() -> str:
    return os.path.dirname(__file__)

def get_config_file_path() -> str:
    # 상위 디렉토리의 conf/ 디렉토리를 봐야 함.
    current_path: str =  get_script_path()
    parent_path: str = os.path.dirname(current_path)
    return parent_path + "/conf/parser.json"


def get_config_opt(conf_path: str) -> dict | None:

    print("conf_path : {}".format(conf_path))
    if os.path.exists(conf_path) is False:
        print("config file is None")
        return None

    try:
        with open(conf_path, "r") as f:
            config: dict = json.load(f)

        obj: any = config.get("bot_token")
        if obj is None or obj == "":
            print("Wrong bot token")
            return None

        obj = config.get("bot_server")
        if obj is None or obj == 0:
            print("Wrong bot channel id")
            return None

        obj = config.get("bot_channel")
        if obj is None or obj == 0:
            print("Wrong bot channel id")
            return None

        obj = config.get("alert_repeat")
        if obj is None or obj < 1:
            print("Wrong alert repeat time")
            return None

        obj = config.get("schedule")
        if obj is None or obj < 1:
            print("Wrong schedule time")
            return None

        if ('telegram' in config) and config.get("telegram_use") == True:
            obj = config["telegram"].get("bot_token")
            if obj is None or obj == "":
                print("no telegram bot token")
                return None
            obj = config["telegram"].get("chat_id")
            if obj is None or obj == 0:
                print("no telegram chat id")
                return None

        obj = config["monitoring"]
        if "monitoring" not in config:
            print("cnanot find Monitoring object")

        if "player" not in config["monitoring"]:
            print("cannot find Monitoring player")
            return None

        if "guild" not in config["monitoring"]:
            print("cannot find Monitoring guild")
            return None

    except Exception as e:
        print("error to parse get config")
        print(e)
        return None

    return config


def set_config(config_dict, config_file=get_config_file_path()) -> bool:
    try:
        with open(config_file, "w") as f:
            json.dump(config_dict, f, indent=4)
    except Exception as e:
        print("config file save error")
        print(e)
        return False
    return True


