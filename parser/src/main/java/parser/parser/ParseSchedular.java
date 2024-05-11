package parser.parser;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import parser.schedule.ScheduleRunner;

public class ParseSchedular {

    private int startSec = 30;
    private int repeatSec = 60;

    public ParseSchedular() {
        this.startSec = 30;
        // 150초 (2분 30초마다)
        this.repeatSec = 150;
    }

    public ParseSchedular(int startSec, int repeatSec) {
        this.startSec = startSec;
        this.repeatSec = repeatSec;
    }

    public void start() {
        Runnable scheduleRunnable = new ScheduleRunner();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(scheduleRunnable, this.startSec, this.repeatSec, TimeUnit.SECONDS);

    }

}