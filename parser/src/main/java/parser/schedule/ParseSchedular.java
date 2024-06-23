package parser.schedule;

import java.time.LocalDateTime;
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

    public ParseSchedular(TelegramBot tgBot, Database db) {
        this.tgBot = tgBot;
        this.db = db;
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

    public void getGrowCastleData() {
        System.out.println("Hello World!");
        LeaderboardDB leaderboardDB = new LeaderboardDB(db);

        // parse Leaderboard player data
        List<LeaderboardBaseEntity> leaderboardData = ParseLeaderboard.player(tgBot).parseLeaderboards();
        if (leaderboardData == null) {
            System.out.println("Leaderboard Player Data Parse Error");
            tgBot.sendMsg("Leaderboard Player Data Parse Error");
            return ;
        }
        leaderboardDB.insertLeaderboards(leaderboardData, LeaderboardType.PLAYER);
        System.out.println("Player Leaderboard Data Inserted Successfully");

        // parse Leaderboard guild data
        leaderboardData.clear();
        leaderboardData = ParseLeaderboard.guild(tgBot).parseLeaderboards();
        if (leaderboardData == null) {
            System.out.println("Leaderboard Guild Data Parse Error");
            tgBot.sendMsg("Leaderboard Guild Data Parse Error");
            return ;
        }
        leaderboardDB.insertLeaderboards(leaderboardData, LeaderboardType.GUILD);
        System.out.println("Guild Leaderboard Data Inserted Successfully");

        // parse Leaderboard hell data (not implemented yet)
        // leaderboardData.clear();
        // leaderboardData = ParseLeaderboard.hellmode(tgBot).parseLeaderboards();
        // leaderboardDB.insertLeaderboards(leaderboardData, LeaderboardType.HELL);

        // guild (우선 순위권 길드만 파싱한다.) 동일 길드의 2군이하 길드는 제외
        String[] guilds = {"underdog", "sayonara", "RedBridge",
                "Paragonia", "Droplet", "777",
                "SKELETON_SKL", "ShaLom" };

        ParseGuild parseGuild = new ParseGuild(tgBot);
        GuildMemberDB guildMemberDB = new GuildMemberDB(db);
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

}