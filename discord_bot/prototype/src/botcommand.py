
import discord
import config
from datetime import datetime
from typing import Optional
from datetime import datetime
from math import ceil
import time

import db


def channel_check(interaction: discord.Interaction, chat_id: list) -> bool:
    """ 설정한 chat id와 다르면 응답하지 않는다.
    chat_id 값이 0이면, 모든 채널에서 응답하는 것으로 간주한다.
    Args:
        interaction (discord.Interaction): 명령어를 실행한 channel_id
        chat_id (list): 검증할 channel_id list

    Returns:
        bool: 동일한 channel_id인지를 리턴
    """
    if len(chat_id) <= 0:
        return True
    # if interaction.channel.id == chat_id
    if interaction.channel.id in chat_id:
        return True
    return False

async def return_hello(command, interaction: discord.Interaction) -> None:

    if command == "hithere":
        await interaction.response.send_message(f'Hi There, {interaction.user.mention}')
        return
    await interaction.response.send_message("Wrong Command")


async def todo(interaction: discord.Interaction) -> None:
    # if interaction.channel_id != chat_id:
    await interaction.response.send_message("todo")

async def user_add(interaction: discord.Interaction, conf: dict, username: str) -> bool:

    player_data = conf["monitoring"]["player"]
    username_org = username
    username = username.lower()
    if username in player_data:
        await interaction.response.send_message(f'"{username_org}" already exist in monitoring list')
        return False

    conf["monitoring"]["player"][username] = {
        "rank_monitoring": True,
        "alert_user_id": str(interaction.user.id),
        # "alert_user_id": str(interaction.member.id),
        "check": False
    }
    config.set_config(conf)
    await interaction.response.send_message(f'add monitoring "{username_org}" success')
    return True

async def user_del(interaction: discord.Interaction, conf: dict, username: str) -> bool:

    player_data = conf["monitoring"]["player"]
    username_org = username
    username = username.lower()
    if username not in player_data:
        await interaction.response.send_message(f'"{username_org}" not found in monitoring list')
        return False

    del conf["monitoring"]["player"][username]
    config.set_config(conf)
    await interaction.response.send_message(f'delete monitoring "{username_org}" success')
    return True

async def user_list(interaction: discord.Interaction, conf: dict) -> None:

    players = conf["monitoring"]["player"].keys()
    string = "monitoring list\n```\n"
    for player in players:
        string += f"{player}\n"
    string += "```"
    await interaction.response.send_message(string)


async def print_user_info(interaction: discord.Interaction,
        conf: dict,
        username: str) -> None:

    conf_data: Optional[dict] = conf.get("data")
    username_org = username
    username = username.lower()
    if conf_data is None:
        # await interaction.response.send_message(f'no {interaction.user.mention} userdata')
        await interaction.response.send_message(f'no {username_org} userdata')
    if "users" not in conf_data:
        await interaction.response.send_message(f'no {username_org} userdata')

    users_data: Optional[dict] = conf_data.get("users")
    if users_data is None:
        await interaction.response.send_message(f'no {username_org} userdata')

    if username not in users_data:
        await interaction.response.send_message(f'no {username_org} userdata')

    my_data: str = users_data[username]
    current_score = my_data["score"]
    current_rank = my_data["rank"]
    last_wave_time = datetime.fromtimestamp(int(my_data["last_wave_time"]))
    last_wave_time_string = last_wave_time.strftime("%Y-%m-%d %H:%M:%S")
    msg: str = "```\nPlayer name : {}\nScore: {}\nRank: {}\nlast waving time: {}\n".format(username_org, current_score, current_rank, last_wave_time_string)

    if "leaderboard" not in conf_data:
        msg += "```"
        await interaction.response.send_message(msg)
        return

    leaderboards = conf_data["leaderboard"]
    obj: int = 0
    if current_rank <= 3:
        obj = leaderboards.get("r1")
        msg += "1st : {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r2")
        msg += "2nd: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r3")
        msg += "3rd: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r4")
        msg += "4th: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r5")
        msg += "5th: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r6")
        msg += "5th: {} ({})\n".format(obj, obj - current_score)
    elif current_rank <= 5:
        obj = leaderboards.get("r3")
        msg += "3rd: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r4")
        msg += "4th: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r5")
        msg += "5th: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r6")
        msg += "6th: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r10")
        msg += "10th: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r11")
        msg += "11th: {} ({})\n".format(obj, obj - current_score)
    elif current_rank <= 10:
        obj = leaderboards.get("r4")
        msg += "4th: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r5")
        msg += "5th: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r6")
        msg += "6th: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r10")
        msg += "10th: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r11")
        msg += "11th: {} ({})\n".format(obj, obj - current_score)
    else:
        obj = leaderboards.get("r5")
        msg += "5th: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r10")
        msg += "10th: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r50")
        msg += "50th: {} ({})\n".format(obj, obj - current_score)
        obj = leaderboards.get("r51")
        msg += "51th: {} ({})\n".format(obj, obj - current_score)

    msg += "\n"
    if "history" in my_data:
        my_history = my_data["history"]
        msg += "last_update_time : {}".format(my_history["last_update_time"])
        ago_score = 0
        history_times = list(my_history.keys())
        for history_time in history_times:
            if ago_score == 0:
                msg += "{} : {}({})\n".format(
                    history_time,
                    my_history[history_time]["score"],
                    my_history[history_time]["rank"]
                )
                continue
            else:
                msg += "{} : {}({}) [{}]\n".format(
                    history_time,
                    my_history[history_time]["score"],
                    my_history[history_time]["rank"],
                    ago_score - my_history[history_time]["score"]
                )
            ago_score = my_history[history_time]
    msg += "```"
    await interaction.response.send_message(msg)


async def user_ok(interaction: discord.Interaction,
        conf: dict,
        username: str) -> None:

    player_data = conf["monitoring"]["player"]
    username = username.lower()
    if username not in player_data:
        await interaction.response.send_message(f'"{username}" not found in monitoring list')
        return

    current_check: bool = player_data[username]["check"]
    if current_check == True:
        await interaction.response.send_message(f'already "{username}" check is True')
        return

    conf["monitoring"]["player"][username]["check"] = True
    config.set_config(conf)
    await interaction.response.send_message(f'"{username}" check stat change True')

async def user_notok(interaction: discord.Interaction,
        conf: dict,
        username: str) -> None:

    player_data = conf["monitoring"]["player"]
    username = username.lower()
    if username not in player_data:
        await interaction.response.send_message(f'"{username}" not found in monitoring list')
        return

    current_check: bool = player_data[username]["check"]
    if current_check == False:
        await interaction.response.send_message(f'already "{username}" check is False')
        return

    conf["monitoring"]["player"][username]["check"] = False
    config.set_config(conf)
    await interaction.response.send_message(f'"{username}" check stat change False')


async def parse_stat(interaction: discord.Interaction,
        conf: dict,
        stat: bool) -> None:

    current_check: bool = conf.get("parse_stop")
    if current_check == stat:
        await interaction.response.send_message("parse_stop object is already {}".format(current_check))
        return

    conf["parse_stop"] = stat
    config.set_config(conf)
    await interaction.response.send_message("parse_stop object change to {}".format(stat))


async def alias_add(interaction: discord.Interaction,
        conf: dict,
        username: str) -> None:

    alias_data = conf["alias"]
    alias_user = str(interaction.user.id)
    lower_username = username.lower()

    if alias_user in alias_data:
        conf["alias"][alias_user] = lower_username
        config.set_config(conf)
        await interaction.response.send_message(f'modify alias "{username}" success')
        return

    conf["alias"][alias_user] = lower_username
    config.set_config(conf)
    await interaction.response.send_message(f'add alias "{username}" success')


async def alias_del(interaction: discord.Interaction,
        conf: dict
    ) -> None:

    alias_data = conf["alias"]
    alias_user = str(interaction.user.id)

    if alias_user not in alias_data:
        await interaction.response.send_message('not found user in alias list')
        return

    del conf["alias"][alias_user]
    config.set_config(conf)
    await interaction.response.send_message('delete alias success')



async def print_history(interaction: discord.Interaction,
        username: str,
        conf: dict,
        db_parser: db.ParsePlayer,
        show_shart: bool,
        show_all: bool
) -> None:

    if db_parser is None:
        await interaction.response.send_message("error, contact to developer")
        return

    if username is None:
        await interaction.response.send_message("wrong username, contact to developer")
        return

    if username == "":
        userid = str(interaction.user.id)
        alias_data = conf["alias"]
        if userid not in alias_data:
            await interaction.response.send_message("alias not found, input username or add alias first")
            return

        username = alias_data[userid]

    history = db_parser.get_history(username)
    if history is None:
        await interaction.response.send_message("error, contact to developer")
        return

    string = ""

    embeds = []
    history_len = len(history)

    if history_len == 0:
        title = "'{}' user data\n\n".format(username)
        string = "'{}' user not found\n\n".format(username)
        embed = discord.Embed(title=title)
        embed.description = (string)
        embeds.append(embed)
    else:
        history_init_row = history.pop(0)
        history_len -= 1
        start_index = 0
        showlen = 5

        if username[0] == '!' or username[0] == '#':
            for data in history:
                if data["name"] == "":
                    continue
                username = data["name"]
                break

        if show_all == False:
            difflen = 2
            if history_len <= 24:
                difflen = 1
            elif history_len % 24 == 0:
                start_index = (history_len // 24) - 2
            else:
                start_index = (history_len // 24) - 1
            showlen = start_index + difflen

        if show_shart:
            for i in range(start_index, showlen, 1):
                title = "'{}' user data, {} day\n\n".format(username, i + 1)
                history_arr = history[24*i:24*(i+1)]
                string = ""
                if i == 0:
                    string += "initialize data\n```\n"
                    string += db.print_history_chart_row(history_init_row)
                    string += "```\n"
                string += db.get_history_chart(history_arr)
                embed = discord.Embed(title=title)
                embed.description = (string)
                embeds.append(embed)
        else:
            # for i in range(5):
            for i in range(start_index, showlen, 1):
                if history_len < 24*(i):
                    break
                title = "'{}' user data, {} day\n\n".format(username, i + 1)
                string = ""
                if i == 0:
                    string += "initialize data\n```\n"
                    string += db.print_history_string_row(history_init_row)
                    string += "```\n"
                history_arr = history[24*i:24*(i+1)]
                string += db.get_history_string(history_arr)
                embed = discord.Embed(title=title)
                embed.description = (string)
                embeds.append(embed)

    await interaction.response.send_message(embeds=embeds)


async def print_leaderboard(interaction: discord.Interaction,
        db_parser: db.ParsePlayer,
        show_rank: int
) -> None:

    if db_parser is None:
        await interaction.response.send_message("error, contact to developer")
        return

    leaderboards = db_parser.get_current_leaderboard()
    if leaderboards is None:
        await interaction.response.send_message("error, contact to developer")
        return

    if len(leaderboards) == 0:
        await interaction.response.send_message("leaderboard not found")
        return

    show_length= min(len(leaderboards), show_rank)
    string = "```\n"
    # for i in range(5):
    for i in range(show_length):
        data = leaderboards[i]
        string += "{:2d}. {:6d} | {}\n".format(data[0], data[2], data[1])

    string += "```\n"
    title = "current leaderboard"
    embed = discord.Embed(title=title)
    embed.description = (string)
    await interaction.response.send_message(embed=embed)


