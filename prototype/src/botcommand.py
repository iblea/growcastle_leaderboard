
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

async def user_add(interaction: discord.Interaction, user: str) -> bool:

    await interaction.response.send_message(f'add monitoring "{user}" success')
    return True

async def user_del(interaction: discord.Interaction, user: str) -> bool:

    await interaction.response.send_message(f'delete monitoring "{user}" success')
    return True

async def print_user_info(interaction: discord.Interaction,
        conf: dict,
        username: str) -> None:

    conf_data: Optional[dict] = conf.get("data")
    if conf_data is None:
        await interaction.response.send_message(f'no {interaction.user.mention} userdata')
    if "users" not in conf_data:
        await interaction.response.send_message(f'no {interaction.user.mention} userdata')
    users_data: Optional[dict] = conf_data.get("users")
    if users_data is None:
        await interaction.response.send_message(f'no {interaction.user.mention} userdata')

    if username not in users_data:
        await interaction.response.send_message(f'no {interaction.user.mention} userdata')

    my_data: str = users_data[username]
    current_score = my_data["score"]
    current_rank = my_data["rank"]
    last_wave_time = datetime.fromtimestamp(int(my_data["last_wave_time"]))
    last_wave_time_string = last_wave_time.strftime("%Y-%m-%d %H:%M:%S")
    msg: str = "```\nPlayer name : {}\nScore: {}\nRank: {}\nlast waving time: {}\n".format(username, current_score, current_rank, last_wave_time_string)

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

    current_check: bool = conf["monitoring"]["player"][username]["check"]
    if current_check == True:
        await interaction.response.send_message(f'already "{username}" check is True')
        return

    conf["monitoring"]["player"][username]["check"] = True
    config.set_config(conf)
    await interaction.response.send_message(f'"{username}" check stat change True')

async def user_notok(interaction: discord.Interaction,
        conf: dict,
        username: str) -> None:

    current_check: bool = conf["monitoring"]["player"][username]["check"]
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



async def print_history(interaction: discord.Interaction,
        username: str,
        db_parser: db.ParsePlayer,
        show_shart: bool,
        show_all: bool
) -> None:

    history = db_parser.get_history(username)
    if history is None:
        await interaction.response.send_message("error, contact to developer")
        return

    history_init_row = history.pop(0)
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
        start_index = 0
        showlen = 5

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





