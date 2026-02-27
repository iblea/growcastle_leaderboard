from typing import Optional
import config

import discord
from discord.ext import tasks
from discord import app_commands
import botcommand

import time
import datetime
from json import dumps

# import parse
from config import set_config
import telegram_bot
import db

import copy
import asyncio


client = None
db_parser = None
schedule_channel = None

MY_GUILD_NAME = "underdog"


class DiscordBot(discord.Client):
    discord_guild_object: Optional[discord.Object] = None
    discord_response_chat_id: list = []
    discord_bot_token: str = ""
    schedule_second: int = 3

    next_alert_time: int = 0
    next_parse_time = 0
    config: Optional[dict] = None

    alert_interval = 2
    alert_list: dict = {}
    alert_channel: Optional[int] = None
    crash_channel: Optional[discord.TextChannel] = None
    last_end_season: str = ""
    last_season_expire_start: int = 0
    last_season_expire_end: int = 0

    parse_fail_count = 0

    leaderboard_channel_id: int = 0
    leaderboard_channel: Optional[discord.TextChannel] = None
    last_leaderboard_message_id: Optional[int] = None
    last_leaderboard_update_second: int = -1

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

        self.leaderboard_channel_id = self.config.get("bot_channel_id_leaderboard", 0) or 0

        self.schedule_second = schedule_second

        super().__init__(intents=intents)
        self.tree = app_commands.CommandTree(self)

    def set_alert_ignore_time(self):
        spdate = self.last_end_season.split(" ")[0].split("-")
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
        self.loop.create_task(self._aligned_schedular())
        await self.tree.sync(guild=self.discord_guild_object)

        print("hook set done")


    async def _aligned_schedular(self):
        """create_task를 %3 == 0 초에 맞춰 실행"""
        now = datetime.datetime.now()
        current_second = now.second
        wait_seconds = (3 - (current_second % 3)) % 3
        if wait_seconds > 0:
            await asyncio.sleep(wait_seconds)
        now = datetime.datetime.now()
        print(f"schedular create_task executed at second: {now.second}")
        await self.schedular()


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

            # leaderboard 채널 설정
            if self.leaderboard_channel_id > 0:
                self.leaderboard_channel = self.get_channel(self.leaderboard_channel_id)
                if self.leaderboard_channel:
                    print(f"Leaderboard channel set: {self.leaderboard_channel.name}")
                    await self.find_last_leaderboard_message()
                else:
                    print(f"Warning: Leaderboard channel ID {self.leaderboard_channel_id} not found")

            print('-----------------------------------')
            self.schedular.start()

        print("event set done")


    # discord bot command
    def apply_command(self) -> None:

        @self.tree.command()
        async def userinfo(interaction: discord.Interaction, user: str = "") -> None:
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return

            await botcommand.print_user_info(interaction=interaction, conf=self.config, username=user)

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
            await botcommand.user_add(interaction=interaction, conf=self.config, username=user)

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
            await botcommand.user_del(interaction=interaction, conf=self.config, username=user)

        @self.tree.command()
        async def userlist(interaction: discord.Interaction):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await botcommand.user_list(interaction=interaction, conf=self.config)

        @self.tree.command()
        async def userok(interaction: discord.Interaction, user: str = ""):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await botcommand.user_ok(interaction=interaction, conf=self.config, username=user)

        @self.tree.command()
        async def userno(interaction: discord.Interaction, user: str = ""):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await botcommand.user_notok(interaction=interaction, conf=self.config, username=user)

        @self.tree.command()
        async def info(interaction: discord.Interaction, user: str = "") -> None:
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return

            await botcommand.print_user_info(interaction=interaction, conf=self.config, username=user)

        @self.tree.command()
        async def ok(interaction: discord.Interaction, user: str = ""):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await botcommand.user_ok(interaction=interaction, conf=self.config, username=user)

        @self.tree.command()
        async def o(interaction: discord.Interaction, user: str = ""):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await botcommand.user_ok(interaction=interaction, conf=self.config, username=user)

        @self.tree.command()
        async def no(interaction: discord.Interaction, user: str = ""):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await botcommand.user_notok(interaction=interaction, conf=self.config, username=user)

        # """ admin command
        @self.tree.command()
        async def reboot(interaction: discord.Interaction):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await interaction.response.send_message("봇을 재부팅합니다.")
            if set_config(config_dict=self.config) == False:
                await interaction.response.send_message("저장에 실패했습니다.")
                await self.close()
                await exit(1)

            print("reboot bot")
            await self.close()
            await exit(0)

        @self.tree.command()
        async def get_config(interaction: discord.Interaction):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            print_dict = copy.deepcopy(self.config)
            del print_dict["bot_token"]
            del print_dict["bot_server"]
            del print_dict["bot_channel"]
            del print_dict["telegram"]
            del print_dict["telegram_use"]
            del print_dict["db"]
            await interaction.response.send_message("```\n" + dumps(print_dict, indent=4) + "\n```")

        @self.tree.command()
        async def parse_stop(interaction: discord.Interaction):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await botcommand.parse_stat(interaction=interaction, conf=self.config, stat=True)

        @self.tree.command()
        async def parse_start(interaction: discord.Interaction):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await botcommand.parse_stat(interaction=interaction, conf=self.config, stat=False)
        # """

        @self.tree.command()
        async def helpbot(interaction: discord.Interaction):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            string = """```
/useradd [username]
유저가 웨이브를 3~5분 이상 올리지 않을 때 알림을 받도록 모니터링 리스트에 추가합니다.
이 모니터링은 시즌 리더보드 200위 이내의 유저에게만 적용됩니다.
add monitoring user

/userdel [username]
모니터링 리스트에서 유저를 삭제합니다.
delete monitoring user

/userlist
모니터링 리스트에 있는 유저들을 출력합니다.

/userok [username]
유저의 모니터링 알림을 일시적으로 해제합니다.
유저가 웨이브를 다시 올리기 시작했을 때 자동으로 모니터링이 다시 시작됩니다.

/userinfo [username]
모니터링 중인 유저의 웨이브 정보와 리더보드의 순위권 및 차이를 출력합니다.



username 에는 닉네임, !랭킹, #별칭 등이 들어갈 수 있습니다.
ex ) username, !10 (현 10위 데이터 출력), #별칭 (별칭으로 등록된 닉네임 데이터 출력)

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

/alias_add [username]
/alias_del

별칭을 등록합니다.
별칭 등록 이후 /history, /chart_history 명령어 실행에
닉네임을 입력하지 않을 경우, 별칭에 등록된 닉네임을 사용합니다.

/leaderboard [rank](선택사항)
리더보드의 상위 rank위까지 출력합니다.
rank 미기입 시 20위까지 출력합니다.
```
"""
            await interaction.response.send_message(string)

        @self.tree.command()
        async def chart_history(interaction: discord.Interaction, username: str = ""):
            await history_command(interaction, username, True, False)

        @self.tree.command()
        async def chart_history_all(interaction: discord.Interaction, username: str = ""):
            await history_command(interaction, username, True, True)

        @self.tree.command()
        async def history(interaction: discord.Interaction, username: str = ""):
            await history_command(interaction, username, False, False)

        @self.tree.command()
        async def history_all(interaction: discord.Interaction, username: str = ""):
            await history_command(interaction, username, False, True)

        @self.tree.command()
        async def hs(interaction: discord.Interaction, username: str = ""):
            # history 단축 명령어
            await history_command(interaction, username, False, False)

        @self.tree.command()
        async def hsc(interaction: discord.Interaction, username: str = ""):
            # chart_history 단축 명령어
            await history_command(interaction, username, True, False)

        @self.tree.command()
        async def alias_add(interaction: discord.Interaction, username: str):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return

            if len(username) <= 0:
                await interaction.response.send_message("username is wrong")
                return

            if db.arg_check(username) is False:
                await interaction.response.send_message("username is wrong")
                return

            await botcommand.alias_add(interaction=interaction, conf=self.config, username=username)

        @self.tree.command()
        async def alias_del(interaction: discord.Interaction):
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return
            await botcommand.alias_del(interaction=interaction, conf=self.config)

        @self.tree.command()
        async def leaderboardspace(interaction: discord.Interaction, rank: str = ""):
            await leaderboard_command(interaction, rank, True)

        @self.tree.command()
        async def leaderboard(interaction: discord.Interaction, rank: str = ""):
            await leaderboard_command(interaction, rank, False)

        @self.tree.command()
        async def lbs(interaction: discord.Interaction, rank: str = ""):
            await leaderboard_command(interaction, rank, True)

        @self.tree.command()
        async def lb(interaction: discord.Interaction, rank: str = ""):
            await leaderboard_command(interaction, rank, False)

        @self.tree.command()
        async def lbg(interaction: discord.Interaction, rank: str = ""):
            await guild_leaderboard_command(interaction, rank)



        print("command set done")

        async def history_command(
            interaction: discord.Interaction,
            username: str = "",
            show_chart: bool = False,
            show_all: bool = False
        ):
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
                conf=self.config,
                db_parser=db_parser,
                show_chart=show_chart, show_all=show_all
            )

        async def leaderboard_command(
            interaction: discord.Interaction,
            rank: str = "",
            is_nickname_space: bool = False
        ):
            global db_parser
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return

            rank_num = 20
            if len(rank) > 0:
                if db.arg_check_number(rank) is False:
                    await interaction.response.send_message("argument is wrong")
                    return
                rank_num = int(rank)

            if db_parser is None:
                db_parser = db.ParsePlayer(bot=self.alert_channel, config=self.config)

            await botcommand.print_leaderboard(
                interaction=interaction,
                db_parser=db_parser,
                conf=self.config,
                show_rank=rank_num,
                is_nickname_space=is_nickname_space
            )

        async def guild_leaderboard_command(
            interaction: discord.Interaction,
            rank: str = "",
        ):
            global db_parser
            if botcommand.channel_check(
                interaction=interaction,
                chat_id=self.discord_response_chat_id
            ) == False:
                return

            rank_num = 20
            if len(rank) > 0:
                if db.arg_check_number(rank) is False:
                    await interaction.response.send_message("argument is wrong")
                    return
                rank_num = int(rank)

            if db_parser is None:
                db_parser = db.ParsePlayer(bot=self.alert_channel, config=self.config)

            await botcommand.print_guild_leaderboard(
                interaction=interaction,
                db_parser=db_parser,
                show_rank=rank_num,
            )




        print("command common function set done")



    def start_bot(self):
        print("bot start")
        self.run(self.discord_bot_token)


    async def find_last_leaderboard_message(self):
        """leaderboard 채널에서 봇이 작성한 마지막 메시지 찾기"""
        if not self.leaderboard_channel:
            return

        try:
            async for message in self.leaderboard_channel.history(limit=100):
                if message.author.id == self.user.id:
                    self.last_leaderboard_message_id = message.id
                    print(f"Leaderboard message found: {message.id}")
                    return
            print("No leaderboard message found in channel")
        except Exception as e:
            print(f"find_last_leaderboard_message error: {e}")


    async def update_leaderboard_channel(self):
        """leaderboard 채널에 메시지 업데이트 (edit or purge and send)"""
        if not self.leaderboard_channel:
            return

        message = self.get_leaderboard_message()
        if not message:
            return

        try:
            last_msg_id = self.leaderboard_channel.last_message_id
            if last_msg_id and last_msg_id == self.last_leaderboard_message_id:
                old_message = await self.leaderboard_channel.fetch_message(last_msg_id)
                await old_message.edit(content=message)
            else:
                await self.leaderboard_channel.purge(check=lambda m: m.author.id == self.user.id)
                new_message = await self.leaderboard_channel.send(message)
                self.last_leaderboard_message_id = new_message.id
        except Exception as e:
            print(f"update_leaderboard_channel error: {e}")


    def get_leaderboard_message(self):
        """leaderboard 채널에 표시할 메시지 생성"""
        global db_parser
        if db_parser is None:
            db_parser = db.ParsePlayer(bot=self.alert_channel, config=self.config)

        leaderboards = db_parser.get_current_leaderboard()
        if leaderboards is None or len(leaderboards) == 0:
            return None

        # monitoring player 목록
        monitoring_players = list(self.config.get("monitoring", {}).get("player", {}).keys())

        # 내 정보 찾기 (monitoring player 중 leaderboard에 있는 유저)
        my_data_list = []
        diff_score = 0
        for data in leaderboards:
            if data[1].lower() in monitoring_players:
                my_data_list.append(data)
                if diff_score == 0:
                    diff_score = data[2]

        if not my_data_list:
            diff_score = leaderboards[0][2]

        now = datetime.datetime.now()
        result = "```\n"

        # 길드 리더보드 1~6위
        guild_leaderboards = db_parser.get_current_guild_leaderboard()
        if guild_leaderboards and len(guild_leaderboards) > 0:
            # Underdog 길드 점수 찾기
            underdog_score = 0
            for gdata in guild_leaderboards:
                if gdata[1].lower() == MY_GUILD_NAME:
                    underdog_score = gdata[2]
                    break

            show_guild_len = min(len(guild_leaderboards), 6)
            for i in range(show_guild_len):
                gdata = guild_leaderboards[i]
                if gdata[1].lower() == MY_GUILD_NAME:
                    result += "{:2d}. {:>7d} | {}\n".format(gdata[0], gdata[2], gdata[1])
                else:
                    diff = underdog_score - gdata[2]
                    result += "{:2d}. {:>7d} | {} ({})\n".format(gdata[0], gdata[2], gdata[1], diff)
            result += "----------\n"

        # 1~15위
        for data in leaderboards:
            rank = data[0]
            if 1 <= rank <= 15:
                result += "{:2d}. {:6d} | {} ({})\n".format(rank, data[2], data[1], diff_score - data[2])

        result += "----------\n"

        # 내 정보 + 주변 ±2 랭킹
        if my_data_list:
            for my_data in my_data_list:
                my_rank = my_data[0]

                # 주변 ±2 랭킹 찾기
                nearby_upper = []
                nearby_lower = []
                for data in leaderboards:
                    r = data[0]
                    if my_rank - 2 <= r <= my_rank - 1:
                        nearby_upper.append(data)
                    elif my_rank + 1 <= r <= my_rank + 2:
                        nearby_lower.append(data)

                nearby_upper.sort(key=lambda x: x[0])
                nearby_lower.sort(key=lambda x: x[0])

                my_score = my_data[2]
                for data in nearby_upper:
                    result += "{:2d}. {:6d} | {} ({})\n".format(data[0], data[2], data[1], my_score - data[2])

                result += "{:2d}. {:6d} | {}\n".format(my_data[0], my_data[2], my_data[1])

                for data in nearby_lower:
                    result += "{:2d}. {:6d} | {} ({})\n".format(data[0], data[2], data[1], my_score - data[2])
        else:
            result += "-\n"
            result += "----------\n"

        # x등까지 남은 wave
        if my_data_list:
            my_rank = my_data_list[0][0]
            my_score = my_data_list[0][2]
            guild_rank = "-"
            guild_name = self.config.get("monitoring", {}).get("guild", {}).get("name", "")
            if guild_name and guild_leaderboards:
                for gdata in guild_leaderboards:
                    if gdata[1].lower() == guild_name.lower():
                        guild_rank = gdata[0]
                        break
            season_day_str = ""
            season_start_str = self.config.get("data", {}).get("seasons", {}).get("start", "")
            if season_start_str:
                kst_tz = datetime.timezone(datetime.timedelta(hours=9))
                season_start_date = datetime.datetime.strptime(season_start_str, "%Y-%m-%d %H:%M:%S").date()
                current_kst_date = datetime.datetime.now(kst_tz).date()
                day_num = (current_kst_date - season_start_date).days + 1
                if 1 <= day_num <= 5:
                    season_day_str = " day: {}".format(day_num)
            result += "---------- me: ({}, {}){}\n".format(my_rank, guild_rank, season_day_str)

            # 내 등수에 따른 표시 등수 필터링
            if my_rank >= 70:
                target_ranks = []
            else:
                target_ranks = [1, 2, 3, 4, 5, 6, 10, 11, 50, 51]
                if my_rank <= 3:
                    target_ranks = [r for r in target_ranks if r not in [10, 11]]
                if my_rank > 3:
                    target_ranks = [r for r in target_ranks if r not in [1, 2]]
                if my_rank > 5:
                    target_ranks = [r for r in target_ranks if r not in [1, 2, 3]]
                if my_rank > 15:
                    target_ranks = [r for r in target_ranks if r not in [4, 5, 6]]
                if my_rank > 35:
                    target_ranks = [r for r in target_ranks if r not in [10, 11]]
                if my_rank <= 20:
                    target_ranks = [r for r in target_ranks if r not in [50, 51]]

            rank_scores = {}
            for data in leaderboards:
                if data[0] in target_ranks:
                    rank_scores[data[0]] = data[2] - my_score

            display_ranks = [r for r in target_ranks if r in rank_scores]
            for i in range(0, len(display_ranks), 2):
                chunk = display_ranks[i:i+2]
                parts = ["{:>2}: ({:>7})".format(r, rank_scores[r]) for r in chunk]
                result += "  ".join(parts) + "\n"

        result += f"updated: {now.strftime('%Y-%m-%d %H:%M:%S')}\n"
        result += "```"
        return result


    async def parse_growcastle_api(self):
        if self.config.get("parse_stop") == True:
            return

        # db_use = False if self.config["db"]["port"] == 0 else True
        # if db_use == True and db_parser is None:
        #     db_parser = db.ParsePlayer(bot=self.alert_channel, config=self.config)

        # parser = parse.ParsePlayer(bot=self.alert_channel, config=self.config)
        parser = db.ParsePlayer(bot=self.alert_channel, config=self.config)
        # parse_stat = False
        parse_stat = await parser.parse_leaderboard(curr_time=int(time.time()))
        # if db_use == True:
        #     parse_stat = await db_parser.parse_leaderboard(curr_time=int(time.time()))
        # else:
        #     parse_stat: bool = await parser.parse_leaderboard(curr_time=int(time.time()))
        # print("parse_stat : {}".format(parse_stat))

        if parse_stat != True:
            self.parse_fail_count +=  1
            if self.parse_fail_count > 4:
                await self.alert_channel.send("parse growcastle api fail {} count".format(self.parse_fail_count))

            # 1분에 6번, 10분에 60번 (10분이상 파싱 연속 실패 시 알림)
            if self.parse_fail_count > 60:
                print("parse fail count over 60")
                self.config["parse_stop"] = True
                self.alert_list = {}
                set_config(config_dict=self.config)
                await self.alert_channel.send("parse stop, fail over 60")
            return

        self.parse_fail_count = 0
        self.alert_list = parser.get_alert_list()
        self.config = parser.get_config()
        set_config(config_dict=self.config)
        if self.last_end_season != self.config["data"]["seasons"]["end"]:
            self.last_end_season = self.config["data"]["seasons"]["end"]
            self.set_alert_ignore_time()

        # parse guild
        # guild alarm (guild alarm은 길드 파싱한 후 최초 1회에 한에서만 동작함)


    # n초마다 돌면서 crash 상태인지 확인
    # @tasks.loop(seconds=5.0)
    @tasks.loop(seconds=3)
    async def schedular(self):

        curr_time = time.time()

        # initialize
        if self.next_alert_time == 0 and self.next_parse_time == 0:
            print("initialize")
            self.next_alert_time = curr_time + self.alert_interval
            self.next_parse_time = curr_time + self.config.get("schedule")

            # parse initialize
            await self.parse_growcastle_api()
            return

        # set alert channel
        if self.alert_channel is None:
            print("set alert channel")
            self.alert_channel = super().get_channel(self.config["bot_channel"][0])
            await self.alert_channel.send("initialize")

        if self.crash_channel is None:
            crash_channel_id = self.config.get("bot_channel_crash", 0)
            if crash_channel_id:
                self.crash_channel = super().get_channel(crash_channel_id)
            if self.crash_channel is None:
                self.crash_channel = self.alert_channel

        # 15초 부근마다 leaderboard 채널 업데이트
        if self.leaderboard_channel is not None:
            current_second = datetime.datetime.now().second
            current_segment = current_second // 15
            if self.last_leaderboard_update_second < 0 or \
               self.last_leaderboard_update_second != current_segment:
                self.last_leaderboard_update_second = current_segment
                await self.update_leaderboard_channel()

        if self.config.get("parse_stop") == True:
            return

        if curr_time >= self.next_parse_time:
            # parse player
            await self.parse_growcastle_api()

            self.next_parse_time = curr_time + self.config.get("schedule")

        if curr_time < self.next_alert_time:
            return

        # if self.get_alert_ignore_time(cur_time=curr_time) == True:
        #     return

        # print("alert")
        telegram_use: bool = self.config["telegram_use"]
        # alarm
        alert_keys = self.alert_list.keys()
        for key in alert_keys:
            user = self.alert_list[key]
            username: str = key
            if username not in self.config["monitoring"]["player"]:
                del self.alert_list[key]
                continue
            if self.config["monitoring"]["player"][username]["check"] == True:
                continue

            msg: str = "user : {} is crashed".format(key)
            await self.crash_channel.send("<@"+ user["user"] + "> " + msg)

            if telegram_use == True:
                parseTime_datetime = datetime.datetime.fromtimestamp(user["last_wave_time"])
                telegram_bot.tg_lock.acquire()
                telegram_bot.tg_user[user["username"]] = parseTime_datetime.strftime("%Y-%m-%d %H:%M:%S")
                telegram_bot.tg_lock.release()


        self.next_alert_time = time.time() + self.alert_interval






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
