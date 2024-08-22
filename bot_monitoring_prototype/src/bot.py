from typing import Optional
import config

import discord
from discord.ext import tasks
from discord import app_commands
import botcommand

import time
import datetime
from json import dumps

from config import set_config
import db

import copy


client = None
db_parser = None
schedule_channel = None


class DiscordBot(discord.Client):
    discord_guild_object: Optional[discord.Object] = None
    discord_response_chat_id: int = -1
    discord_bot_token: str = ""
    schedule_second: int = 3

    next_alert_time: int = 0
    next_parse_time = 0
    config: Optional[dict] = None

    alert_interval = 3
    alert_list: list = []
    alert_channel: Optional[int] = None
    last_end_season: str = ""
    last_season_expire_start: int = 0
    last_season_expire_end: int = 0

    parse_fail_count = 0
    my_username: str = "Ib"

    def __init__(self,
            config: dict,
            schedule_second: int = 3,
            # intents: discord.Intents = discord.Intents.default()
            intents = discord.Intents().all()
    ) -> None:
        self.config =config

        server_id: int = self.config.get("bot_server")
        self.discord_bot_token = self.config.get("bot_token")
        self.discord_guild_object = discord.Object(id=server_id)
        self.discord_response_chat_id = self.config.get("bot_channel")

        self.schedule_second = schedule_second

        super().__init__(intents=intents)
        self.tree = app_commands.CommandTree(self)

    def set_alert_ignore_time(self):
        spdate = self.last_end_season.split("-")
        specific_time = datetime.datetime(
            year=int(spdate[0]),
            month=int(spdate[1]),
            day=int(spdate[2]),
            hour=23,
            minute=45,
            second=0)
        unix_time = int(time.mktime(specific_time.timetuple()))
        self.last_season_expire_start = unix_time

        specific_time = datetime.datetime(
            year=int(spdate[0]),
            month=int(spdate[1]),
            day=int(spdate[2]),
            hour=23,
            minute=55,
            second=0)
        unix_time = int(time.mktime(specific_time.timetuple()))
        self.last_season_expire_end = unix_time


    def get_alert_ignore_time(self, cur_time) -> bool:
        """시즌 마지막 날의 23시 45 ~ 23시 55분 사이에는 검사하지 않는다.
        """
        if cur_time >= self.last_season_expire_start and cur_time < self.last_season_expire_end:
            return True
        return False


    async def setup_hook(self) -> None:
        self.tree.copy_global_to(guild=self.discord_guild_object)
        print(f'Create in as {self.user} (ID: {self.user.id})')
        await self.tree.sync(guild=self.discord_guild_object)

        print("hook set done")


    # discord event
    def apply_event(self) -> None:
        @self.event
        async def close():
            print("Bot is closing...")
            await exit(0)

        @self.event
        async def on_ready():
            print(f'Logged in as {self.user} (ID: {self.user.id})')
            print('-----------------------------------')

        print("event set done")


# discord bot command
    def apply_command(self) -> None:

        @self.tree.command()
        async def helpbot(interaction: discord.Interaction):
            string = """```
/history [username] (mobile)
show history of username
hour | rank, score, diff | per, horn, dhorn, cjump
시즌시간 | 랭킹, 총합 웨이브, 변동량 | 클리어 횟수, 호른점프, 더블호른점프, 크리스탈점프
전일과 금일의 시즌 데이터를 출력한다. (show ago and today data)

/history_all [username] (mobile)
show all history of username
hour | rank, score, diff | per, horn, dhorn, cjump
시즌시간 | 랭킹, 총합 웨이브, 변동량 | 클리어 횟수, 호른점프, 더블호른점프, 크리스탈점프
현 시즌의 모든 데이터를 출력한다. (show all data of this season)


/chart_history [username] (pc)
show history chart of username
전일과 금일의 시즌 데이터를 차트 형식으로 출력한다. (show ago and today data)

/chart_history_all [username] (pc)
show all history chart of username
현 시즌의 모든 데이터를 차트 형식으로 출력한다. (show all data of this season)
```
"""
            await interaction.response.send_message(string)


        @self.tree.command()
        async def chart_history(interaction: discord.Interaction, username: str):
            global db_parser
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return

            if db_parser is None:
                db_parser = db.ParsePlayer(bot=self.alert_channel, config=self.config)

            if db.arg_check(username) is False:
                await interaction.response.send_message("username is wrong")
                return

            await botcommand.print_history(
                interaction=interaction,
                username=username,
                db_parser=db_parser,
                show_shart=True,
                show_all=False
            )

        @self.tree.command()
        async def chart_history_all(interaction: discord.Interaction, username: str):
            global db_parser
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return

            if db_parser is None:
                db_parser = db.ParsePlayer(bot=self.alert_channel, config=self.config)

            if db.arg_check(username) is False:
                await interaction.response.send_message("username is wrong")
                return

            await botcommand.print_history(
                interaction=interaction,
                username=username,
                db_parser=db_parser,
                show_shart=True, show_all=True
            )

        @self.tree.command()
        async def history(interaction: discord.Interaction, username: str):
            global db_parser
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return

            if db_parser is None:
                db_parser = db.ParsePlayer(bot=self.alert_channel, config=self.config)

            if db.arg_check(username) is False:
                await interaction.response.send_message("username is wrong")
                return
            await botcommand.print_history(
                interaction=interaction,
                username=username,
                db_parser=db_parser,
                show_shart=False, show_all=False
            )

        @self.tree.command()
        async def history_all(interaction: discord.Interaction, username: str):
            global db_parser
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return

            if db_parser is None:
                db_parser = db.ParsePlayer(bot=self.alert_channel, config=self.config)

            if db.arg_check(username) is False:
                await interaction.response.send_message("username is wrong")
                return
            await botcommand.print_history(
                interaction=interaction,
                username=username,
                db_parser=db_parser,
                show_shart=False, show_all=True
            )


        print("command set done")



    def start_bot(self):
        print("bot start")
        self.run(self.discord_bot_token)




def main():
    global client
    config_file_path: str = config.get_config_file_path()
    conf: dict= config.get_config_opt(config_file_path)
    if conf is None:
        print("error: cannot parse conf")

    # intents = discord.Intents.default()
    # intents = discord.Intents().all()
    client = DiscordBot(
        config=conf,
        schedule_second=5
    )

    client.apply_event()
    client.apply_command()

    client.start_bot()
    # client.run(bot_token)


if __name__ == "__main__":
    main()
