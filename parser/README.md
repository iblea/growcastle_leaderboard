# GrowCastle API Parser

성키우기 공식 API를 바탕으로 하여, 게임에 접속하지 않고도 현 시즌의 리더보드 정보를 출력하고
유저 데이터 정보를 취합해 사용자의 플레이어 및 길드의 패턴(점프 시간, 평균 웨이브) 을 계산, 게임 크래시를 빠르게 체크해 사용자에게 피드백하는 서비스를 목표로 합니다.


## 공식 API 정보

공식 API 정보는 아래의 포맷을 따릅니다. \
<https://raongames.com/growcastle/restapi/season/[date]/[type]> \
`[date]` 칸에는 `now` 또는 `yyyy-mm-dd` 의 날짜 형식이 입력될 수 있습니다.

Now를 기준으로 URL을 기술합니다.

- 플레이어 시즌 리더보드 정보
    - <https://raongames.com/growcastle/restapi/season/now/players>

- 특정 플레이어에 대한 정보
    - <https://raongames.com/growcastle/restapi/season/now/players/[PlayerName]>

- 길드 시즌 리더보드 정보
    - <https://raongames.com/growcastle/restapi/season/now/guilds>

- 특정 길드의 길드원에 대한 정보 (길드 시즌 리더보드에 없을 경우 표기되지 않을 수 있음.)
    - <https://raongames.com/growcastle/restapi/season/now/guilds>

- 무한 웨이브
    - <https://raongames.com/growcastle/restapi/season/now/hell/>



## 동작 설명

- 매분마다 API를 요청하여 Player, Guild의 정보를 저장합니다.
    - 플레이어 leaderboard URL
        - `https://raongames.com/growcastle/restapi/season/yyyy-mm-dd/players`
    - Guild leaderboard URL
        - `https://raongames.com/growcastle/restapi/season/yyyy-mm-dd/guilds`
    - 특정 Guild의 플레이어 정보
        - `https://raongames.com/growcastle/restapi/season/yyyy-mm-dd/guilds/guildname`


- 모니터링하는 길드명은 다음과 같습니다.
    - 동일 길드의 2군 이하는 모니터링하지 않습니다.

- underdog
    - underdog2
- sayonara
    - sayonara 2
- redbrigde
- Paragonia
- Droplet
- 7 7 7
- SKELETON_SKL
- Polish_Polish
- ShaLom
- Rus50


### API 정보

- result 에서 시작된다.
- date에는 시즌의 시작 시간과 끝 시간이 GMT 로 저장된다.
- list에는 플레이어 또는 길드의 정보가 저장된다.


### Table 정보

-테이블은 한국 시간(KST) 기준으로 시즌이 끝나는 날의 23시 50 ~ 25시 59분에 초기화된다.

- 플레이어 리더보드 정보 : Player_Leaderboard
- 길드 리더보드 정보 : Guild_Leaderboard
- 길드 정보 : Guild_`<GuildName>`

- 길드 모니터링 정보 : MONITORING
- 봇 토큰 정보 : TOKEN


