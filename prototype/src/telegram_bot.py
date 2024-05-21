import telegram
import asyncio
import threading
from time import sleep


tg_bot: any = None
tg_chatid: any = None
tg_user: dict = {}
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


def telegram_thread():
    global tg_user
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    while True:
        # WARNING: sleep 1초시 초당 1회 요청이라 429 too many requests 로 차단될 가능성 있음.
        sleep(1)
        if not tg_user:
            continue
        userkeys = list(tg_user.keys())
        msg = ""
        for key in userkeys:
            loop = asyncio.get_event_loop()
            msg += "User : {} is crashed, cur_wave: {}, ago_wave: {}" \
                .format(key, tg_user[key]["cur_wave"], tg_user[key]["ago_wave"])
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
    telegram_use: bool = config.get("telegram_use")

    if telegram_use == False:
        return
    if "telegram" not in config:
        return

    tg_bot = telegram.Bot(token=config["telegram"]["bot_token"])
    tg_chatid = config["telegram"]["chat_id"]
    tg_alert_repeat = config["telegram"]["alert_repeat"]
    print("init telegram")

    tg_user["telegram bot initialize. this is init check message"] = {"cur_wave": 0, "ago_wave": 0}

    tg_thread = threading.Thread(target=telegram_thread)
    tg_thread.daemon = True
    tg_thread.start()



