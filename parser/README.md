# GrowCastle API Parser

성키우기 공식 API에서 제공하는 정보를 수집하여, 게임에 접속하지 않고도 현 시즌의 리더보드
정보를 출력하고 유저 데이터 정보를 취합해 사용자의 플레이어 및 길드의 패턴(점프 시간,
평균 웨이브)을 분석하여 DB에 저장하는 것을 목표로 합니다.


## 공식 API 정보

공식 API 정보는 아래의 포맷을 따릅니다. \
[https://raongames.com/growcastle/restapi/season/[date]/[type]](https://raongames.com/growcastle/restapi/season/now/players) \
`[date]` 칸에는 `now` 또는 `yyyy-mm-dd` 의 날짜 형식이 입력될 수 있습니다.

Now를 기준으로 URL을 기술합니다.

- 플레이어 시즌 리더보드 정보
    - <https://raongames.com/growcastle/restapi/season/now/players>

- 특정 플레이어에 대한 정보
    - [https://raongames.com/growcastle/restapi/season/now/players/[PlayerName]](https://raongames.com/growcastle/restapi/season/now/players/Ib)

- 길드 시즌 리더보드 정보
    - <https://raongames.com/growcastle/restapi/season/now/guilds>

- 특정 길드의 길드원에 대한 정보 (길드 시즌 리더보드에 없을 경우 표기되지 않을 수 있음.)
    - [https://raongames.com/growcastle/restapi/season/now/guilds/[GuildName]](https://raongames.com/growcastle/restapi/season/now/guilds/Underdog)

- 무한 웨이브
    - <https://raongames.com/growcastle/restapi/season/now/hell/>


## 빌드 및 실행

#### 요구 사항
- Java 17
- PostgreSQL 16
  - DB 및 테이블 생성 필요.
  - 테이블 정보는 [`sql/table.sql`](../sql/table.sql) 파일 참조


#### 사전 준비
- `src/main/resources/log4j2.properties` 생성
  - sample 파일을 복사하여 생성합니다.
    - `cp -r src/main/resources/log4j2.properties.sample src/main/resources/log4j2.properties`
  - log4j2.properties 파일을 수정하여 원하는 경로에 로그가 저장되도록 설정합니다.

- `src/main/resources/META-INF/persistence.xml` 생성
  - sample 파일을 복사하여 생성합니다.
    - `cp -r src/main/resources/META-INF/persistence.xml.sample src/main/resources/META-INF/persistence.xml`
  - persistence.xml 파일을 수정하여 DB 정보를 입력합니다.

#### 빌드
```sh
./gradlew build
```

## 동작 설명

- 매분마다 API를 요청하여 Player, Guild의 정보를 저장합니다. \
  `yyyy-mm-dd` 에 `now` 를 대체하여 현재의 정보를 가져올 수 있습니다.
    - 플레이어 leaderboard URL
        - `https://raongames.com/growcastle/restapi/season/yyyy-mm-dd/players`
    - Guild leaderboard URL
        - `https://raongames.com/growcastle/restapi/season/yyyy-mm-dd/guilds`
    - 특정 Guild의 플레이어 정보
        - `https://raongames.com/growcastle/restapi/season/yyyy-mm-dd/guilds/guildname`


- 모니터링하는 길드명은 다음과 같습니다.
  - 동일 길드의 2군 이하는 모니터링하지 않습니다.

- underdog
- sayonara
- redbrigde
- paragonia
- droplet
- skeleton_skl
- shalom


### Table 정보
-테이블은 한국 시간(KST) 기준으로 시즌이 끝나는 날의 23시 50분에 초기화됩니다.

- 리더보드 정보 : `Leaderboard_<type>`
  - 플레이어 리더보드 정보 : `Leaderboard_Player`
  - 길드 리더보드 정보 : `Leaderboard_Guild`
  - 무한 웨이브 리더보드 정보 : `Leaderboard_Hell`

- 히스토리 정보 : `History_<type>`
  - 플레이어 히스토리 정보 : `History_Player`
  - 길드 히스토리 정보 : `History_Guild`

- 길드 멤버의 모니터링 정보 : `guild_member_wave`
- 봇 토큰 정보 : TOKEN


