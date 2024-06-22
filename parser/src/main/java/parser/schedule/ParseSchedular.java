package parser.schedule;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import parser.db.Database;
// import parser.schedule.ScheduleRunner;
import parser.telegram.TelegramBot;

public class ParseSchedular {

    private TelegramBot tgBot;
    private Database db;
    private static final int PARSERSECDURINGSTART = 20;
    private static final int PARSESECDURINGEND = 30;
    // private static final int REPEATSEC = 300;
    private static final int REPEATSEC = 3;

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

    private void showTime() {
        LocalDateTime now = LocalDateTime.now();
        System.out.printf("current : %d:%d:%d%n",
            now.getHour(), now.getMinute(), now.getSecond());
    }

    // Existing code...
    public void testFunc() {
        System.out.println("Hello Test!");
        showTime();
        try{
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        showTime();
    }

    public void getGrowCastleData() {
        System.out.println("Hello World!");
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
        executor.scheduleAtFixedRate(this::testFunc, 0, REPEATSEC, TimeUnit.SECONDS);
    }

}