# 요구사항

- `>= python3.10`
- `>= pip23.0.1`
- `discord`
- `telegram`
- `venv`


# 초기 venv 세팅

```bash
python3 -m venv gcleaderboard
```


# 패키지 설치

`requirements_freeze.txt` 파일이 존재한다면 아래 명령어로 설치 가능하다.
```bash
source discordbot/bin/activate
gcleaderboard/bin/pip3 install -r requirements_freeze.txt
deactivate
```

없다면 라이브러리를 손수 설치해야 한다.
```bash
source discordbot/bin/activate
gcleaderboard/bin/pip3 install requests
gcleaderboard/bin/pip3 install discord
gcleaderboard/bin/pip3 install python-telegram-bot --upgrade
gcleaderboard/bin/pip3 freeze > requirements_freeze.txt
deactivate
```


# 실행
```bash
./start.sh
```