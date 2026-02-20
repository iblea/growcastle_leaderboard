import telegram
import asyncio
import threading
from time import sleep
from config import set_config


tg_bot: any = None
tg_chatid: any = None
tg_user: dict = {}
tg_config: dict = None
tg_lock = threading.Lock()
tg_alert_repeat: int = 1

# bot will not be able to send more than 20 messages per minute to the same group.
# telegram은 분당 20 req 이상의 요청을 보낼 수 없다.
# 또한 초당 두개 이상의 메시지를 보낼 수 없다. (이러한 burst 형식의 메시지를 보낼 시 429 오류 발생)
async def telegram_msg_send(msg_str):
    global tg_bot
    global tg_chatid
    global tg_alert_repeat

    if tg_bot is None:
        return
    if tg_chatid < 0:
        return

    if msg_str == "":
        return

    for _ in range(tg_alert_repeat):
        await tg_bot.sendMessage(chat_id=tg_chatid, text=msg_str)
        # await asyncio.sleep(0.5)


def handle_ok_command():
    """모든 모니터링 유저의 check를 True로 설정하여 알림을 중지 (discord + telegram)"""
    global tg_config, tg_user

    if tg_config is None:
        return "config not found"

    player_data = tg_config.get("monitoring", {}).get("player", {})
    if not player_data:
        return "no monitoring players"

    changed = []
    for username in player_data:
        if player_data[username]["check"] == False:
            tg_config["monitoring"]["player"][username]["check"] = True
            changed.append(username)

    # 대기 중인 알림 제거
    tg_lock.acquire()
    tg_user.clear()
    tg_lock.release()

    set_config(tg_config)

    if changed:
        return "alert disabled for: {}".format(", ".join(changed))
    else:
        return "all players already checked"


def telegram_thread():
    global tg_user
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    last_update_id = 0

    # 기존 업데이트 소비 (봇 시작 전에 쌓인 메시지 무시)
    try:
        updates = loop.run_until_complete(tg_bot.getUpdates())
        if updates:
            last_update_id = updates[-1].update_id
    except Exception:
        pass

    while True:
        # WARNING: sleep 1초시 초당 1회 요청이라 429 too many requests 로 차단될 가능성 있음.
        sleep(1)

        # 텔레그램 업데이트 폴링 (명령어 수신)
        try:
            updates = loop.run_until_complete(
                tg_bot.getUpdates(offset=last_update_id + 1, timeout=0)
            )
            for update in updates:
                last_update_id = update.update_id
                if update.message and update.message.text:
                    text = update.message.text.strip()
                    if text == "/ok":
                        result = handle_ok_command()
                        loop.run_until_complete(telegram_msg_send(result))
        except Exception:
            pass

        if not tg_user:
            continue
        userkeys = list(tg_user.keys())
        msg = ""
        for key in userkeys:
            loop = asyncio.get_event_loop()
            value = tg_user[key]
            if value is None:
                msg += "prototype bot information : {}".format(key)
            else:
                msg += "User : {} is crashed, last waving time : {}".format(key, tg_user[key])
            msg += "\n"
            tg_lock.acquire()
            del tg_user[key]
            tg_lock.release()

        # 초당 2번 이상 메시지를 날리면 오류 난다.
        loop.run_until_complete(telegram_msg_send(msg))

    # 사실상 죽은 코드
    loop.close()

def start_telegram_bot(config: dict):
    global tg_bot
    global tg_chatid
    global tg_alert_repeat
    global tg_user
    global tg_config
    telegram_use: bool = config.get("telegram_use")

    if telegram_use == False:
        return
    if "telegram" not in config:
        return

    tg_config = config
    tg_bot = telegram.Bot(token=config["telegram"]["bot_token"])
    tg_chatid = config["telegram"]["chat_id"]
    tg_alert_repeat = config["telegram"]["alert_repeat"]
    print("init telegram")

    tg_user["telegram bot initialize."] = None

    tg_thread = threading.Thread(target=telegram_thread)
    tg_thread.daemon = True
    tg_thread.start()



