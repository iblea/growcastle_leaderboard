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
// import parser.schedule.ScheduleRunner;
import parser.telegram.TelegramBot;

public class ParseSchedular {

    private TelegramBot tgBot;
    private Database db;
    private static final int PARSERSECDURINGSTART = 20;
    private static final int PARSESECDURINGEND = 30;
    private static final int REPEATSEC = 300;
    // private static final int REPEATSEC = 3;

    // TODO: 길드는 이후 DB에서 가져오는 것으로 변경할 예정
    private String[] guilds = { "underdog", "sayonara", "redbridge",
                "paragonia", "droplet", "777",
                "skeleton_skl", "shalom" };
    // private String[] guilds = { "underdog" };


    private LocalDateTime startSeasonDate;
    private LocalDateTime endSeasonDate;

    public ParseSchedular(TelegramBot tgBot, Database db) {
        this.tgBot = tgBot;
        this.db = db;
        this.endSeasonDate = null;
    }

    public void start() {
        // Runnable scheduleRunnable = new ScheduleRunner();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        try {
            this.initializeWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("initializeWait error");
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
        System.out.println("initialize Wait");
        while (true) {
            LocalDateTime now = LocalDateTime.now();
            // test code
            // if (true) { break; }

            if (now.getMinute() % 5 != 0) {
                Thread.sleep(500);
                continue;
            }

            if (now.getSecond() >= PARSERSECDURINGSTART &&
                now.getSecond() < PARSESECDURINGEND) {
                break;
            }

            Thread.sleep(500);
        }
        System.out.println("initialize Wait Done");
    }

    private LocalDateTime getNow5Minutes() {
        ZoneId kstZoneId = ZoneId.of("Asia/Seoul");
        LocalDateTime now = LocalDateTime.now(kstZoneId);
        int minute = ((now.getMinute() / 5) * 5);
        return now.withMinute(minute).withSecond(0).withNano(0);
    }

    public void getGrowCastleData() {
        LocalDateTime now = getNow5Minutes();
        if (checkSeasonEnd(now)) {
            return;
        }
        if (this.startSeasonDate != null) {
            System.out.println("Delete ago Start Season Date : " + this.startSeasonDate);
            deleteDatabaseUntilDate(this.startSeasonDate);
        }
        // getParseLeaderboards(now);
        getParseGuilds(now);
    }

    /**
     * 시즌 종료 시간을 체크한다.
     * 시즌종료일의 23시 50분 부터는 파싱하지 않는다.
     * @return
     */
    public Boolean checkSeasonEnd(LocalDateTime now) {

        if (this.startSeasonDate == null || this.endSeasonDate == null) {
            System.out.println("End Season Date is null");
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
        // 5분 단위로 체크 (53분이면 (53 / 5) * 5 = 50)
        if (now.getMinute() < this.endSeasonDate.getMinute()) {
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
        LeaderboardDB leaderboardDB = new LeaderboardDB(db);
        leaderboardDB.deleteLeaderboardsUntilDate(date);

        GuildMemberDB guildMemberDB = new GuildMemberDB(db);
        for (String guildName : guilds) {
            guildMemberDB.deleteGuildDataUntilDate(date, guildName);
        }
    }

    public void getParseLeaderboards(LocalDateTime now) {
        LeaderboardDB leaderboardDB = new LeaderboardDB(db);

        // parse Leaderboard player data
        List<LeaderboardBaseEntity> leaderboardData = ParseLeaderboard.player(tgBot, now).parseLeaderboards();
        if (leaderboardData == null) {
            System.out.println("Leaderboard Player Data Parse Error");
            tgBot.sendMsg("Leaderboard Player Data Parse Error");
            return ;
        }
        leaderboardDB.insertLeaderboards(leaderboardData, LeaderboardType.PLAYER);
        System.out.println("Player Leaderboard Data Inserted Successfully");

        // parse Leaderboard guild data
        leaderboardData.clear();
        leaderboardData = ParseLeaderboard.guild(tgBot, now).parseLeaderboards();
        if (leaderboardData == null) {
            System.out.println("Leaderboard Guild Data Parse Error");
            tgBot.sendMsg("Leaderboard Guild Data Parse Error");
            return ;
        }
        leaderboardDB.insertLeaderboards(leaderboardData, LeaderboardType.GUILD);
        System.out.println("Guild Leaderboard Data Inserted Successfully");

        // parse Leaderboard hell data (not implemented yet)
        // leaderboardData.clear();
        // leaderboardData = ParseLeaderboard.hellmode(tgBot, now).parseLeaderboards();
        // leaderboardDB.insertLeaderboards(leaderboardData, LeaderboardType.HELL);
    }

    // guild는 30분 단위로 파싱한다.
    public void getParseGuilds(LocalDateTime now) {
        // guild (우선 순위권 길드만 파싱한다.) 동일 길드의 2군이하 길드는 제외
        GuildMemberDB guildMemberDB = new GuildMemberDB(db);
        ParseGuild parseGuild = new ParseGuild(tgBot);
        for (String guildName : guilds) {
            List<GuildMember> guildData = parseGuild.parseGuildByName(guildName);
            if (guildData == null) {
                System.out.println("Guild (" + guildName + ") Data Parse Error");
                tgBot.sendMsg("Guild (" + guildName + ") Data Parse Error");
                return ;
            }
            guildMemberDB.insertGuildMembers(guildData, guildName);
            System.out.println("Guild (" + guildName + ") Data Inserted Successfully");
        }
    }

    private void showTime(String msg) {
        LocalDateTime now = LocalDateTime.now();
        System.out.printf("%s | current : %d:%d:%d%n",
            msg, now.getHour(), now.getMinute(), now.getSecond());
    }

    // Existing code...
    private void testFunc() {
        System.out.println("Hello Test!");
        showTime("ago");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        showTime("after");
    }

}