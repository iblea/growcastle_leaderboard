
import sys
import config
# from bot import DiscordBot
import bot
import telegram_bot

def start_bot(conf) -> bot.DiscordBot:

    discord_bot = bot.DiscordBot(
        config=conf
    )
    discord_bot.apply_event()
    discord_bot.apply_command()
    # discord_bot.run(bot_token)
    discord_bot.start_bot()
    return discord_bot



def main() -> int:
    print("GrowCastle Crash Alarm Script Start")

    config_file_path = config.get_config_file_path()
    conf: dict= config.get_config_opt(config_file_path)
    if conf is None:
        print("error: cannot parse conf")
        return 1

    if conf["parse_stop"] == True:
        conf["parse_stop"] = False
        config.set_config(conf)

    telegram_bot.start_telegram_bot(conf)

    start_bot(
        conf=conf
    )
    return 0


if __name__ == "__main__":
    ret = main()
    sys.exit(ret)