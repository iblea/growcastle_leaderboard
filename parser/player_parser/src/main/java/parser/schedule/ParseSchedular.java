package parser.schedule;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import parser.db.Database;
import parser.db.GuildMemberWaveDB;
import parser.db.HistoryDB;
import parser.db.LeaderboardDB;
import parser.db.SeasonDataDB;
import parser.entity.GuildMemberWave;
import parser.entity.LeaderboardBaseEntity;
import parser.entity.LeaderboardPlayer;
import parser.entity.SeasonData;
import parser.parser.LeaderboardType;
import parser.parser.ParseGuild;
import parser.parser.ParseLeaderboard;
import parser.telegram.TelegramBot;

public class ParseSchedular {

    private static Logger logger = LogManager.getLogger(ParseSchedular.class);

    private TelegramBot tgBot;
    private Database db;

    // TODO: 길드는 이후 DB에서 가져오는 것으로 변경할 예정
    private String[] guilds = {"underdog", "sayonara", "redbridge",
        "paragonia", "droplet",
        "skeleton_skl", "shalom"};
    // private String[] guilds = { "underdog" };

    private SeasonData seasonData = null;

    private LocalDateTime nextParseTime = null;
    // private static final int PARSE_TERM_SEC = 3600;
    private static final int PARSE_TERM_SEC = 300;

    // 시즌 종료일 프로그램 종료 시각 (crontab에 의해 23:52에 재시작)
    private static final int EXIT_HOUR = 23;
    private static final int EXIT_MINUTE = 51;
    private static final int EXIT_SECOND = 10;

    private LeaderboardDB leaderboardDB = null;
    private HistoryDB historyDB = null;
    private GuildMemberWaveDB guildMemberWaveDB = null;

    public ParseSchedular(TelegramBot tgBot, Database db) {
        this.tgBot = tgBot;
        this.db = db;
        this.seasonData = new SeasonData();
        // this.nextParseTime = getUpdateNextTime(getNowKST());
        this.nextParseTime = getDivide5MinutesPlus5Minutes(getNowKST());
        logger.info("Next History Parse Time : {}", this.nextParseTime);
        this.setDatabaseConnection();
    }

    public boolean setDatabaseConnection() {
        if (this.db == null) {
            this.db = new Database("growcastle");
            if (!db.connectEntityManagerFactory()) {
                return false;
            }
        }
        if (this.leaderboardDB == null) {
            this.leaderboardDB = new LeaderboardDB(this.db);
        }
        if (this.historyDB == null) {
            this.historyDB = new HistoryDB(this.db);
        }
        if (this.guildMemberWaveDB == null) {
            this.guildMemberWaveDB = new GuildMemberWaveDB(this.db);
        }
        return true;
    }

    private void clearAllEntityManager() {
        if (this.leaderboardDB != null) {
            this.leaderboardDB.clearEntityManager();
            this.leaderboardDB.closeEntityManager();
        }
        if (this.historyDB != null) {
            this.historyDB.clearEntityManager();
            this.historyDB.closeEntityManager();
        }
        if (this.guildMemberWaveDB != null) {
            this.guildMemberWaveDB.clearEntityManager();
            this.guildMemberWaveDB.closeEntityManager();
        }
    }

    public void start() {
        // Runnable scheduleRunnable = new ScheduleRunner();
        tgBot.sendMsg("bot scheduler start");

        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (!isSecondDivide10()) {
                    return;
                }
                scheduleMain();
                // testFunc();

                // try {
                //     getGrowCastleData();
                // } catch (Exception e) {
                //     logger.error("Scheduler Error : {}", e.getMessage());
                //     StackTraceElement[] s = e.getStackTrace();
                //     for(StackTraceElement stack : s){
                //         logger.error("\tat {}", stack);
                //     }
                //     System.exit(1);
                // }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private boolean isSecondDivide10() {
        LocalDateTime now = getNowKST();
        return (now.getSecond() % 10 == 0);
    }

    public LocalDateTime divide5Minutes(LocalDateTime timeobj) {
        int minute = timeobj.getMinute();
        if (minute == 0) {
            return timeobj.withMinute(0).withSecond(30).withNano(0);
        }
        minute = (minute / 5) * 5;
        return timeobj.withMinute(minute).withSecond(30).withNano(0);
    }

    public LocalDateTime divideHour(LocalDateTime timeobj) {
        return timeobj.withMinute(0).withSecond(0).withNano(0);
    }

    private LocalDateTime getNowKST() {
        ZoneId kstZoneId = ZoneId.of("Asia/Seoul");
        return LocalDateTime.now(kstZoneId).withNano(0);
    }

    private LocalDateTime getDivide5MinutesPlus5Minutes(LocalDateTime timeobj) {
        LocalDateTime nowPlus5Minutes = timeobj.plusSeconds(PARSE_TERM_SEC);
        return divide5Minutes(nowPlus5Minutes);
    }
    private LocalDateTime getUpdateNextTime(LocalDateTime timeobj) {
        LocalDateTime nowPlus1Hour = timeobj.plusHours(1);
        return divideHour(nowPlus1Hour);
    }

    public void scheduleMain() {
        getGrowCastleData();
        clearAllEntityManager();
    }

    public void exitProgram(String reason) {
        logger.info("Program Exit : {}", reason);
        if (tgBot != null) {
            tgBot.sendMsg("Program Exit : " + reason);
        }
        System.exit(0);
    }

    public boolean isAfterExitTime(LocalDateTime now) {
        LocalDateTime exitTime = now.toLocalDate().atTime(EXIT_HOUR, EXIT_MINUTE, EXIT_SECOND);
        return !now.isBefore(exitTime);
    }

    // Main Scheduler
    public void getGrowCastleData() {
        logger.debug("Get GrowCastle Data Scheduler Start");
        LocalDateTime now = getNowKST();
        // LocalDateTime nowDivide5Minutes = divide5Minutes(now);
        boolean updateInform = (now.isEqual(this.nextParseTime) || now.isAfter(this.nextParseTime));
        // boolean updateInform = true;

        if (checkSeasonEnd(now)) {
            logger.debug("Season End, Only Leaderboard Data Parse");
            getParseLeaderboards(false);
            if (isAfterExitTime(now)) {
                exitProgram("Season End");
            }
            return;
        }

        // parse Leaderboard data
        getParseLeaderboards(updateInform);

        // parse Guild data
        if (!updateInform) {
            return;
        }

        if (this.seasonData.isNotNull()) {
            logger.info("Delete ago Start Season Date : {}", this.seasonData.getStartDate());
            deleteDatabaseUntilDate(this.seasonData.getStartDate());
        }
        // getParseGuilds(now);
        // this.nextParseTime = getUpdateNextTime(nowDivideHour);
        this.nextParseTime = getDivide5MinutesPlus5Minutes(now);
    }

    public void parseSeasonData(LocalDateTime now) {
        logger.debug("parse Season Data (start/end)");
        SeasonDataDB seasonDataDB = new SeasonDataDB(this.db);
        if (this.seasonData.isNull()) {
            SeasonData data = seasonDataDB.findSeasonData();
            seasonDataDB.closeEntityManager();
            if (data != null) {
                this.seasonData = null;
                this.seasonData = new SeasonData(data);
                // 이미 파싱된 데이터가 시즌 종료 시간을 지나지 않았을 경우 다시 파싱하지 않는다.
                if (!isAfterSeasonEnd(now, this.seasonData.getEndDate())) {
                    logger.info("Season Data is already parsed : {}", this.seasonData.getEndDate());
                    return;
                }
                this.seasonData.setNull();
            }
        } else {
            if (!isAfterSeasonEnd(now, this.seasonData.getEndDate())) {
                logger.info("Season Data is already existed : {}", this.seasonData.getEndDate());
                return;
            }
            this.seasonData.setNull();
        }

        logger.info("parse new Season Data");
        ParseLeaderboard parseAPI = ParseLeaderboard.player(tgBot, now);
        parseAPI.parseLeaderboards();
        this.seasonData.setStartDate(parseAPI.getStartSeasonDate());
        this.seasonData.setEndDate(parseAPI.getEndSeasonDate());
        if (this.seasonData.isNull()) {
            logger.error("Season Date Parse Error");
            tgBot.sendMsg("Season Date Parse Error");
            return;
        }
        if (!isAfterSeasonEnd(now, this.seasonData.getEndDate())) {
            logger.info("update season data");
            seasonDataDB.updateSeasonData(seasonData);
            seasonDataDB.closeEntityManager();
        } else {
            // 10분 단위로 계산을 진행하므로
            // 파싱한 시각이 시즌 종료 시간을 지났을 수가 있다.
            logger.info("ended data : {}", this.seasonData.getEndDate());
            this.seasonData.setNull();
        }
    }

    public boolean isAfterSeasonEnd(LocalDateTime now, LocalDateTime endSeasonDate) {
        LocalDateTime endSeason50Minute = endSeasonDate.withMinute((endSeasonDate.getMinute() / 10) * 10);
        if (now.isBefore(endSeason50Minute)) {
            return false;
        }

        logger.debug("after season {} > {}", now, endSeasonDate);
        return true;
    }

    /**
     * 시즌 종료 시간을 체크한다. 시즌종료일의 23시 50분 부터는 히스토리 정보는 파싱하지 않는다.
     *
     * @return
     */
    public boolean checkSeasonEnd(LocalDateTime now) {

        if (this.seasonData.isNull()) {
            logger.debug("season data is null, parse season data");
            parseSeasonData(now);
            if (this.seasonData.isNull()) {
                logger.info("Season is End");
                return true;
            }
        }

        LocalDateTime endSeasonDate = this.seasonData.getEndDate();
        if (!isAfterSeasonEnd(now, endSeasonDate)) {
            return false;
        }

        logger.debug("Season End, Delete History Data");
        deleteDatabaseUntilDate(endSeasonDate);
        // +5 계산해서 다음 시즌을 체크해도 되지만,
        // 서버로부터 정확한 데이터를 체크하기 위해 null로 데이터를 초기화하고 다시 파싱해 가져온다.
        logger.debug("Season End, season data set null");
        this.seasonData.setNull();
        return true;
    }

    public void deleteDatabaseUntilDate(LocalDateTime date) {
        setDatabaseConnection();
        this.historyDB.deleteHistoryUntilDate(date);
        this.guildMemberWaveDB.deleteGuildMemberWaveUntilDate(date);
    }

    public void getParseLeaderboards(boolean updateInform) {
        setDatabaseConnection();
        List<LeaderboardBaseEntity> leaderboardData = null;

        // parse Leaderboard guild data
        leaderboardData = ParseLeaderboard.guild(
                this.tgBot, getNowKST()
        ).parseLeaderboards();
        insertGuildData(leaderboardData, updateInform);
        leaderboardData.clear();

        // parse Leaderboard player data
        leaderboardData = ParseLeaderboard.player(
                this.tgBot, getNowKST()
        ).parseLeaderboards();
        insertPlayerData(leaderboardData, updateInform);
        leaderboardData.clear();

        // parse Leaderboard hell data (not implemented yet)
        // leaderboardData.clear();
        // leaderboardData = ParseLeaderboard.hellmode(tgBot, now).parseLeaderboards();
        // leaderboardDB.insertLeaderboards(leaderboardData, LeaderboardType.HELL, false);
    }

    public void getParseGuilds(LocalDateTime curTime) {
        // guild (우선 순위권 길드만 파싱한다.) 동일 길드의 2군이하 길드는 제외
        ParseGuild parseGuild = new ParseGuild(this.tgBot, this.seasonData.getSeasonName(), curTime);

        int failCount = 0;
        List<GuildMemberWave> allGuildMembers = new LinkedList<>();
        for (String guildName : guilds) {
            List<GuildMemberWave> guildData = parseGuild.parseGuildByName(guildName);
            if (guildData == null || guildData.isEmpty()) {
                logger.error("Guild ({}) Data Parse Error", guildName);
                this.tgBot.sendMsg("Guild (" + guildName + ") Data Parse Error");
                continue;
            }
            allGuildMembers.addAll(guildData);
        }

        if (allGuildMembers.isEmpty()) {
            logger.error("Guild Data Parse Error");
            this.tgBot.sendMsg("Guild Data Parse Error");
            return;
        }

        boolean insertStat = false;
        insertStat = this.guildMemberWaveDB.insertGuildMemberWaves(allGuildMembers);
        if (!insertStat) {
            logger.error("Guild Data Insert Error");
            this.tgBot.sendMsg("Guild Data Insert Error : " + failCount);
        }
    }

    public void insertGuildData(List<LeaderboardBaseEntity> leaderboardData, boolean updateInform) {
        if (leaderboardData == null || leaderboardData.isEmpty()) {
            logger.error("Leaderboard Guild Data Parse Error");
            this.tgBot.sendMsg("Leaderboard Guild Data Parse Error");
            return;
        }
        boolean result = this.leaderboardDB.updateLeaderboards(leaderboardData, LeaderboardType.GUILD);
        if (!result) {
            logger.error("Leaderboard Guild Data Update Error");
            this.tgBot.sendMsg("Leaderboard Guild Data Update Error");
            return;
        }
        if (updateInform) {
            result = this.historyDB.insertHistory(
                    leaderboardData, LeaderboardType.GUILD, this.seasonData.getSeasonName(), this.nextParseTime
            );
            if (!result) {
                logger.error("History Guild Data Insert Error");
                this.tgBot.sendMsg("History Guild Data Insert Error");
            }
        }
    }

    public void insertPlayerData(List<LeaderboardBaseEntity> leaderboardData, boolean updateInform) {
        if (leaderboardData == null || leaderboardData.isEmpty()) {
            logger.error("Leaderboard Player Data Parse Error");
            this.tgBot.sendMsg("Leaderboard Player Data Parse Error");
            return;
        }
        boolean result = this.leaderboardDB.updateLeaderboardsPlayerTracking(leaderboardData);
        if (!result) {
            logger.error("Leaderboard Player Data Update Error");
            this.tgBot.sendMsg("Leaderboard Player Data Update Error");
            return;
        }
        if (updateInform) {
            List<LeaderboardPlayer> data = this.leaderboardDB.getLeaderboardPlayersAll();
            result = this.historyDB.insertHistoryPlayerTracking(
                    data, this.seasonData.getSeasonName(), this.nextParseTime
            );
            if (!result) {
                logger.error("History Player Data Insert Error");
                this.tgBot.sendMsg("History Player Data Insert Error");
            } else {
                logger.info("History Player Data Insert Success");
                this.leaderboardDB.setInitializeTrackedData();
            }
        }
    }

    private void testFunc() {
        // System.out.println("start time : " + LocalDateTime.now());
        // for (int i = 0; i < 15; i++) {
        //     try {
        //         System.out.printf("testFunc : [%d]%n", i);
        //         Thread.sleep(1000);

        //     } catch (InterruptedException e) {
        //         e.printStackTrace();
        //         System.out.println("sleep error in testFunc");
        //         Thread.currentThread().interrupt();
        //     }
        // }
        // System.out.println("end time : " + LocalDateTime.now());
        logger.debug("testFunc");
        LocalDateTime now = getNowKST();
        LocalDateTime nowDivideHour = divideHour(now);
        System.err.println("now : " + now);
        System.err.println("nowDivideHour : " + nowDivideHour);
        // HistoryPlayer historydPlayer = new HistoryPlayer(new LeaderboardBaseEntity(1, "test", 100));
        // logger.debug("rank : {}", historydPlayer.getRank());
    }

}
