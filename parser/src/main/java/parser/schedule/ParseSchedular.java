package parser.schedule;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.LinkedList;

import parser.db.Database;
import parser.db.LeaderboardDB;
import parser.db.SeasonDataDB;
import parser.db.GuildMemberWaveDB;
import parser.db.HistoryDB;
import parser.entity.GuildMemberWave;
import parser.entity.LeaderboardBaseEntity;
import parser.entity.LeaderboardPlayer;
import parser.entity.SeasonData;
import parser.parser.LeaderboardType;
import parser.parser.ParseGuild;
import parser.parser.ParseLeaderboard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// import parser.schedule.ScheduleRunner;
import parser.telegram.TelegramBot;

public class ParseSchedular {

    private static Logger logger = LogManager.getLogger(ParseSchedular.class);

    private TelegramBot tgBot;
    private Database db;

    // TODO: 길드는 이후 DB에서 가져오는 것으로 변경할 예정
    private String[] guilds = { "underdog", "sayonara", "redbridge",
                "paragonia", "droplet",
                "skeleton_skl", "shalom" };
    // private String[] guilds = { "underdog" };


    private SeasonData seasonData = null;

    private LocalDateTime nextParseTime = null;
    private static final int PARSE_TERM_SEC = 900;

    public ParseSchedular(TelegramBot tgBot, Database db) {
        this.tgBot = tgBot;
        this.db = db;
        this.seasonData = new SeasonData();
        this.nextParseTime = getDivide15MinutesPlus15Minutes(getNowKST());
        logger.info("Next History Parse Time : {}", this.nextParseTime);
    }

    public void start() {
        // Runnable scheduleRunnable = new ScheduleRunner();
        tgBot.sendMsg("bot scheduler start");

        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                if (! isSecondDivide10()) {
                    return ;
                }
                getGrowCastleData();
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

    public LocalDateTime divide15Minutes(LocalDateTime timeobj) {
        int minute = timeobj.getMinute();
        if (minute == 0) {
            return timeobj.withMinute(0).withSecond(0).withNano(0);
        }
        minute = (minute / 15) * 15;
        return timeobj.withMinute(minute).withSecond(0).withNano(0);
    }

    private LocalDateTime getNowKST() {
        ZoneId kstZoneId = ZoneId.of("Asia/Seoul");
        return LocalDateTime.now(kstZoneId).withNano(0);
    }

    private LocalDateTime getDivide15MinutesPlus15Minutes(LocalDateTime timeobj) {
        LocalDateTime nowPlus15Minutes = timeobj.plusSeconds(PARSE_TERM_SEC);
        return divide15Minutes(nowPlus15Minutes);
    }


    // Main Scheduler
    public void getGrowCastleData() {
        logger.debug("Get GrowCastle Data Scheduler Start");
        LocalDateTime now = getNowKST();
        LocalDateTime nowDivide15Minute = divide15Minutes(now);
        boolean updateInform = (nowDivide15Minute.isEqual(this.nextParseTime) || nowDivide15Minute.isAfter(this.nextParseTime));
        // boolean updateInform = true;

        if (checkSeasonEnd(now)) {
            logger.debug("Season End, Only Leaderboard Data Parse");
            getParseLeaderboards(false);
            return;
        }

        // parse Leaderboard data
        getParseLeaderboards(updateInform);

        // parse Guild data
        if (! updateInform) {
            return ;
        }

        if (this.seasonData.isNotNull()) {
            logger.info("Delete ago Start Season Date : {}", this.seasonData.getStartDate());
            deleteDatabaseUntilDate(this.seasonData.getStartDate());
        }
        getParseGuilds(nowDivide15Minute);
        this.nextParseTime = getDivide15MinutesPlus15Minutes(nowDivide15Minute);
    }

    public void parseSeasonData(LocalDateTime now) {
        logger.debug("parse Season Data (start/end)");
        SeasonDataDB seasonDataDB = new SeasonDataDB(this.db);
        if (this.seasonData.isNull()) {
            SeasonData data = seasonDataDB.findSeasonData();
            if (data != null) {
                this.seasonData = null;
                this.seasonData = new SeasonData(data);
                // 이미 파싱된 데이터가 시즌 종료 시간을 지나지 않았을 경우 다시 파싱하지 않는다.
                if (! isAfterSeasonEnd(now, this.seasonData.getEndDate())) {
                    return ;
                }
                this.seasonData.setNull();
            }
        } else {
            if (! isAfterSeasonEnd(now, this.seasonData.getEndDate())) {
                return ;
            }
            this.seasonData.setNull();
        }

        ParseLeaderboard parseAPI = ParseLeaderboard.player(tgBot, now);
        parseAPI.parseLeaderboards();
        this.seasonData.setStartDate(parseAPI.getStartSeasonDate());
        this.seasonData.setEndDate(parseAPI.getEndSeasonDate());
        if (this.seasonData.isNull()) {
            logger.error("Season Date Parse Error");
            tgBot.sendMsg("Season Date Parse Error");
            return ;
        }
        if (! isAfterSeasonEnd(now, this.seasonData.getEndDate())) {
            seasonDataDB.updateSeasonData(seasonData);
        } else {
            // 10분 단위로 계산을 진행하므로
            // 파싱한 시각이 시즌 종료 시간을 지났을 수가 있다.
            this.seasonData.setNull();
        }
    }

    public boolean isAfterSeasonEnd(LocalDateTime now, LocalDateTime endSeasonDate) {
        LocalDateTime endSeason50Minute = endSeasonDate.withSecond((endSeasonDate.getMinute() / 10) * 10);
        if (now.isBefore(endSeason50Minute)) {
            return false;
        }

        logger.debug("after season {} > {}", now, endSeasonDate);
        return true;
    }

    /**
     * 시즌 종료 시간을 체크한다.
     * 시즌종료일의 23시 50분 부터는 히스토리 정보는 파싱하지 않는다.
     * @return
     */
    public boolean checkSeasonEnd(LocalDateTime now) {

        if (this.seasonData.isNull()) {
            logger.debug("season data is null, parse season data");
            parseSeasonData(now);
        }

        LocalDateTime endSeasonDate = this.seasonData.getEndDate();
        if (! isAfterSeasonEnd(now, endSeasonDate)) {
            return false;
        }

        deleteDatabaseUntilDate(endSeasonDate);
        // +5 계산해서 다음 시즌을 체크해도 되지만,
        // 서버로부터 정확한 데이터를 체크하기 위해 null로 데이터를 초기화하고 다시 파싱해 가져온다.
        this.seasonData.setNull();
        return true;
    }

    public void deleteDatabaseUntilDate(LocalDateTime date) {
        HistoryDB historyDB = new HistoryDB(this.db);
        historyDB.deleteHistoryUntilDate(date);

        GuildMemberWaveDB guildMemberWaveDB = new GuildMemberWaveDB(this.db);
        guildMemberWaveDB.deleteGuildMemberWaveUntilDate(date);
    }

    public void getParseLeaderboards(boolean updateInform) {
        LeaderboardDB leaderboardDB = new LeaderboardDB(this.db);
        HistoryDB historyDB = new HistoryDB(this.db);
        boolean result;

        List<LeaderboardBaseEntity> leaderboardData = null;

        // parse Leaderboard guild data
        leaderboardData = ParseLeaderboard.guild(
            this.tgBot, getNowKST()
        ).parseLeaderboards();
        if (leaderboardData == null || leaderboardData.isEmpty()) {
            logger.error("Leaderboard Guild Data Parse Error");
            this.tgBot.sendMsg("Leaderboard Guild Data Parse Error");
        } else {
            result = leaderboardDB.updateLeaderboards(leaderboardData, LeaderboardType.GUILD);
            if (! result) {
                logger.error("Leaderboard Guild Data Update Error");
                this.tgBot.sendMsg("Leaderboard Guild Data Update Error");
                return ;
            }
            if (updateInform) {
                result = historyDB.insertHistory(
                    leaderboardData, LeaderboardType.GUILD, this.seasonData.getSeasonName(), this.nextParseTime
                );
                if (! result) {
                    logger.error("History Guild Data Insert Error");
                    this.tgBot.sendMsg("History Guild Data Insert Error");
                }
            }
        }


        // parse Leaderboard player data
        leaderboardData = ParseLeaderboard.player(
            this.tgBot, getNowKST()
        ).parseLeaderboards();

        if (leaderboardData == null || leaderboardData.isEmpty()) {
            logger.error("Leaderboard Player Data Parse Error");
            this.tgBot.sendMsg("Leaderboard Player Data Parse Error");
        } else {
            result = leaderboardDB.updateLeaderboardsPlayerTracking(leaderboardData);
            if (! result) {
                logger.error("Leaderboard Player Data Update Error");
                this.tgBot.sendMsg("Leaderboard Player Data Update Error");
                return ;
            }
            if (updateInform) {
                List<LeaderboardPlayer> data = leaderboardDB.getLeaderboardPlayersAll();
                result = historyDB.insertHistoryPlayerTracking(
                    data, this.seasonData.getSeasonName(), this.nextParseTime
                );
                if (! result) {
                    logger.error("History Player Data Insert Error");
                    this.tgBot.sendMsg("History Player Data Insert Error");
                } else {
                    logger.info("History Player Data Insert Success");
                    leaderboardDB.setInitializeTrackedData();
                }
            }
            leaderboardData.clear();
        }

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
            return ;
        }

        boolean insertStat = false;
        GuildMemberWaveDB guildMemberWaveDB = new GuildMemberWaveDB(this.db);
        insertStat = guildMemberWaveDB.insertGuildMemberWaves(allGuildMembers);
        if (! insertStat) {
            logger.error("Guild Data Insert Error");
            this.tgBot.sendMsg("Guild Data Insert Error : " + failCount);
        }
    }


    public void testFunc() {
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
        LocalDateTime nowDivide15Minute = divide15Minutes(now);
        System.err.println("now : " + now);
        System.err.println("nowDivide15Minute : " + nowDivide15Minute);
        // HistoryPlayer historydPlayer = new HistoryPlayer(new LeaderboardBaseEntity(1, "test", 100));
        // logger.debug("rank : {}", historydPlayer.getRank());
    }

}
