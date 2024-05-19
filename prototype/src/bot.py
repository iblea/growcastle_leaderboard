from typing import Optional

import discord
from discord.ext import tasks
from discord import app_commands
import botcommand

import time
import parse
from config import set_config

from json import dumps


class DiscordBot(discord.Client):
    discord_guild_object: Optional[discord.Object] = None
    discord_response_chat_id: int = -1
    discord_bot_token: str = ""
    schedule_second: int = 1

    next_alert_time: int = 0
    next_parse_time = 0
    config: Optional[dict] = None

    alert_interval = 3
    alert_list: list = []
    alert_channel: Optional[int] = None

    def __init__(self,
            config: dict,
            schedule_second: int = 1,
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


    async def setup_hook(self) -> None:
        self.tree.copy_global_to(guild=self.discord_guild_object)
        self.loop.create_task(self.schedular())
        print(f'Create in as {self.user} (ID: {self.user.id})')
        await self.tree.sync(guild=self.discord_guild_object)

        print("hook set done")


    # discord event
    def apply_event(self) -> None:
        @self.event
        async def close():
            print("Bot is closing...")
            self.schedular.cancel()
            await exit(0)

        @self.event
        async def on_ready():
            print(f'Logged in as {self.user} (ID: {self.user.id})')
            print('-----------------------------------')
            self.schedular.start()

        print("event set done")


    # discord bot command
    def apply_command(self) -> None:
        @self.tree.command()
        async def userinfo(interaction: discord.Interaction) -> None:
            await interaction.response.send_message(f'Hi, {interaction.user.mention}')

        @self.tree.command()
        @app_commands.describe(
            user='Add Monitoring User'
        )
        async def useradd(interaction: discord.Interaction, user: str):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return

            await botcommand.todo(interaction=interaction)

        @self.tree.command()
        @app_commands.describe(
            user='Delete Monitoring User',
        )
        async def userdel(interaction: discord.Interaction, user: str):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await botcommand.todo(interaction=interaction)

        @self.tree.command()
        async def ok(interaction: discord.Interaction):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await botcommand.user_ok(interaction=interaction, conf=self.config, username="Ib")

        @self.tree.command()
        async def reboot(interaction: discord.Interaction):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await interaction.response.send_message("봇을 재부팅합니다.")
            if set_config() == False:
                await interaction.response.send_message("저장에 실패했습니다.")
                await self.close()
                await exit(1)

            await self.close()
            await exit(0)

        @self.tree.command()
        async def get_config(interaction: discord.Interaction):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await interaction.response.send_message(dumps(self.config, indent=4))


        print("command set done")


    def start_bot(self):
        print("bot start")
        self.run(self.discord_bot_token)


    # n초마다 돌면서 crash 상태인지 확인
    # @tasks.loop(seconds=5.0)
    @tasks.loop(seconds=1)
    async def schedular(self):
        curr_time = time.time()

        # initialize
        if self.next_alert_time == 0 and self.next_parse_time == 0:
            print("initialize")
            self.next_alert_time = curr_time + self.alert_interval
            self.next_parse_time = curr_time + self.config.get("schedule")

            # parse initialize
            parser = parse.ParsePlayer(bot=self.alert_channel, config=self.config)
            parse_stat: bool = parser.parse_leaderboard(curr_time=time.time())
            print("parse_stat : {}".format(parse_stat))
            if parse_stat == True:
                self.alert_list = parser.get_alert_list()
                self.config = parser.get_config()
                set_config(config_dict=self.config)
            return

        # set alert channel
        if self.alert_channel is None:
            print("set alert channel")
            self.alert_channel = super().get_channel(self.config["bot_channel"])
            await self.alert_channel.send("initialize")


        if curr_time >= self.next_parse_time:
            print("parse")
            # parse player
            parser = parse.ParsePlayer(bot=self.alert_channel, config=self.config)
            parse_stat = parser.parse_leaderboard(curr_time=time.time())
            print("parse_stat : {}".format(parse_stat))
            if parse_stat == True:
                self.alert_list = parser.get_alert_list()
                self.config = parser.get_config()
                set_config(config_dict=self.config)

            # parse guild

            # guild alarm (guild alarm은 길드 파싱한 후 최초 1회에 한에서만 동작함)

            self.next_parse_time = curr_time + self.config.get("schedule")

        if curr_time < self.next_alert_time:
            return

        print("alert")
        telegram_use: bool = self.config["telegram_use"]
        # alarm
        for user in self.alert_list:
            username: str = user["username"]
            if self.config["monitoring"]["player"][username]["check"] == True:
                continue

            msg: str = "user : {} is crashed\ncurrent_wave : {}\nago_wave : {}" \
                .format(user["username"], user["cur_wave"], user["ago_wave"])
            print(msg)
            await self.alert_channel.send("<@"+ user["user"] + "> " + msg)

            if telegram_use == True:
                # TODO: telegram alert
                continue


        self.next_alert_time = time.time() + self.alert_interval






def main():
    import config
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
