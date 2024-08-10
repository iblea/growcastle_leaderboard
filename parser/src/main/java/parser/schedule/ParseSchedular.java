package parser.schedule;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import parser.db.Database;
import parser.db.LeaderboardDB;
import parser.db.GuildMemberDB;
import parser.entity.GuildMember;
import parser.entity.LeaderboardBaseEntity;
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
    private static final int REPEATSEC = 10;                   // 10초
    // private static final int REPEATSEC = 3;

    // TODO: 길드는 이후 DB에서 가져오는 것으로 변경할 예정
    private String[] guilds = { "underdog", "sayonara", "redbridge",
                "paragonia", "droplet", "777",
                "skeleton_skl", "shalom" };
    // private String[] guilds = { "underdog" };


    private LocalDateTime startSeasonDate;
    private LocalDateTime endSeasonDate;

    private LocalDateTime nextParseTime;
    private static final int PARSE_TERM_SEC = 900;

    public ParseSchedular(TelegramBot tgBot, Database db) {
        this.tgBot = tgBot;
        this.db = db;
        this.endSeasonDate = null;
        this.nextParseTime = getDivide15MinutesPlus15Minutes(getNowKST());
        logger.info("Next History Parse Time : {}", this.nextParseTime);
    }

    public void start() {
        // Runnable scheduleRunnable = new ScheduleRunner();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        try {
            this.initializeWait();
        } catch (InterruptedException e) {
            logger.error("initializeWait error");
            logger.error(e.getMessage());
            tgBot.sendMsg("initializeWait error\n\n" + e.getMessage());
            Thread.currentThread().interrupt();
            return ;
        }
        tgBot.sendMsg("bot scheduler start");

        executor.scheduleAtFixedRate(this::getGrowCastleData, 0, REPEATSEC, TimeUnit.SECONDS);
        // executor.scheduleAtFixedRate(this::testFunc, 0, REPEATSEC, TimeUnit.SECONDS);
        // thread logic
        // executor.scheduleAtFixedRate(() -> {
        //     Thread t = new Thread(this::testFunc);
        //     t.start();
        // }, 0, REPEATSEC, TimeUnit.SECONDS);

    }



    private void initializeWait() throws InterruptedException {
        logger.debug("initialize Wait");
        while (true) {
            LocalDateTime now = LocalDateTime.now();
            // test code
            // if (true) { break; }

            if (now.getSecond() % 10 == 0) {
                break;
            }
            Thread.sleep(1000);
        }
        logger.debug("initialize Wait Done");
    }

    public LocalDateTime divide15Minutes(LocalDateTime timeobj) {
        int minute = timeobj.getMinute();
        if (minute == 0) {
            return timeobj.withMinute(0).withSecond(0).withNano(0);
        }
        minute = (minute/ 15) * 15;
        return timeobj.withMinute(minute).withSecond(0).withNano(0);
    }

    private LocalDateTime getNowKST() {
        ZoneId kstZoneId = ZoneId.of("Asia/Seoul");
        return LocalDateTime.now(kstZoneId);
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

        if (checkSeasonEnd(now)) {
            getParseLeaderboards(false);
            return;
        }

        // parse Leaderboard data
        getParseLeaderboards(updateInform);

        // parse Guild data
        if (! updateInform ) {
            return ;
        }

        if (this.startSeasonDate != null) {
            logger.info("Delete ago Start Season Date : {}", this.startSeasonDate);
            deleteDatabaseUntilDate(this.startSeasonDate);
        }
        getParseGuilds();
        this.nextParseTime = getDivide15MinutesPlus15Minutes(nowDivide15Minute);
    }

    /**
     * 시즌 종료 시간을 체크한다.
     * 시즌종료일의 23시 50분 부터는 히스토리 정보는 파싱하지 않는다.
     * @return
     */
    public boolean checkSeasonEnd(LocalDateTime now) {

        if (this.startSeasonDate == null || this.endSeasonDate == null) {
            logger.info("End Season Date is null");
            ParseLeaderboard parseAPI = ParseLeaderboard.player(tgBot, now);
            parseAPI.parseLeaderboards();
            this.startSeasonDate = parseAPI.getStartSeasonDate();
            this.endSeasonDate = parseAPI.getEndSeasonDate();
        }

        if (now.getYear() != this.endSeasonDate.getYear()) {
            return false;
        }
        if (now.getMonth() != this.endSeasonDate.getMonth()) {
            return false;
        }
        if (now.getDayOfMonth() != this.endSeasonDate.getDayOfMonth()) {
            return false;
        }
        if (now.getHour() != this.endSeasonDate.getHour()) {
            return false;
        }

        // 55분 시즌마감이면 50분부터 파싱하지 않는다.
        if (now.getMinute() < ((this.endSeasonDate.getMinute() / 10) * 10)) {
            return false;
        }

        deleteDatabaseUntilDate(this.endSeasonDate);
        // +5 계산해서 다음 시즌을 체크해도 되지만,
        // 서버로부터 정확한 데이터를 체크하기 위해 null로 데이터를 초기화하고 다시 파싱해 가져온다.
        this.startSeasonDate = null;
        this.endSeasonDate = null;
        return true;
    }

    public void deleteDatabaseUntilDate(LocalDateTime date) {
        LeaderboardDB leaderboardDB = new LeaderboardDB(this.db);
        leaderboardDB.deleteHistoryLeaderboardsUntilDate(date);

        GuildMemberDB guildMemberDB = new GuildMemberDB(this.db);
        for (String guildName : guilds) {
            guildMemberDB.deleteGuildDataUntilDate(date, guildName);
        }
    }

    public void getParseLeaderboards(boolean updateInform) {
        LeaderboardDB leaderboardDB = new LeaderboardDB(this.db);

        // parse Leaderboard player data
        List<LeaderboardBaseEntity> leaderboardData = ParseLeaderboard.player(this.tgBot, getNowKST()).parseLeaderboards();
        if (leaderboardData == null) {
            logger.error("Leaderboard Player Data Parse Error");
            this.tgBot.sendMsg("Leaderboard Player Data Parse Error");
            return ;
        }
        leaderboardDB.updateLeaderboards(leaderboardData, LeaderboardType.PLAYER);
        if (updateInform) {
            leaderboardDB.insertLeaderboards(leaderboardData, LeaderboardType.PLAYER, false);
        }
        // parse Leaderboard guild data
        leaderboardData.clear();
        leaderboardData = ParseLeaderboard.guild(this.tgBot, getNowKST()).parseLeaderboards();
        if (leaderboardData == null) {
            logger.error("Leaderboard Guild Data Parse Error");
            this.tgBot.sendMsg("Leaderboard Guild Data Parse Error");
            return ;
        }
        leaderboardDB.updateLeaderboards(leaderboardData, LeaderboardType.GUILD);
        if (updateInform) {
            leaderboardDB.insertLeaderboards(leaderboardData, LeaderboardType.GUILD, false);
        }

        // parse Leaderboard hell data (not implemented yet)
        // leaderboardData.clear();
        // leaderboardData = ParseLeaderboard.hellmode(tgBot, now).parseLeaderboards();
        // leaderboardDB.insertLeaderboards(leaderboardData, LeaderboardType.HELL, false);
    }

    public void getParseGuilds() {
        // guild (우선 순위권 길드만 파싱한다.) 동일 길드의 2군이하 길드는 제외
        ParseGuild parseGuild = new ParseGuild(this.tgBot);

        for (String guildName : guilds) {
            GuildMemberDB guildMemberDB = new GuildMemberDB(this.db);
            List<GuildMember> guildData = parseGuild.parseGuildByName(guildName);
            if (guildData == null) {
                logger.error("Guild ({}) Data Parse Error", guildName);
                this.tgBot.sendMsg("Guild (" + guildName + ") Data Parse Error");
                continue;
            }
            guildMemberDB.insertGuildMembers(guildData, guildName);
            logger.debug("Guild ({}) Data Inserted Successfully", guildName);
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
        // HistoryPlayer historydPlayer = new HistoryPlayer(new LeaderboardBaseEntity(1, "test", 100));
        // logger.debug("rank : {}", historydPlayer.getRank());
    }

}