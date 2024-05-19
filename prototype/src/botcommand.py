
import discord
import config


def channel_check(interaction: discord.Interaction, chat_id: int) -> bool:
    """ 설정한 chat id와 다르면 응답하지 않는다.
    chat_id 값이 0이면, 모든 채널에서 응답하는 것으로 간주한다.
    Args:
        interaction (discord.Interaction): 명령어를 실행한 channel_id
        chat_id (int): 검증할 channel_id

    Returns:
        bool: 동일한 channel_id인지를 리턴
    """
    if chat_id == 0:
        return True
    if interaction.channel.id == chat_id:
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



