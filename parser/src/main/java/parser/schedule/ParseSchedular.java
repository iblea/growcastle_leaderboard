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
    private final int parseSecDuringStart = 20;
    private final int parseSecDuringEnd = 30;
    // private final int repeatSec = 300;
    private final int repeatSec = 3;

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

            if (now.getSecond() >= this.parseSecDuringStart &&
                now.getSecond() < this.parseSecDuringEnd) {
                break;
            }

            Thread.sleep(500);
        }
        System.out.println("initialize Wait Done");
    }

    // Existing code...
    public void testFunc() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Hello Test!");
        System.out.printf("current : %d:%d:%d\n",
            now.getHour(), now.getMinute(), now.getSecond());
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
            return ;
        }
        tgBot.sendMsg("bot scheduler start");
        executor.scheduleAtFixedRate(this::testFunc, 0, repeatSec, TimeUnit.SECONDS);
    }

}