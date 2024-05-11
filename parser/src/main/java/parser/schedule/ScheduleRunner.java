package parser.schedule;

public class ScheduleRunner implements Runnable {

    private String startDate;
    private String endDate;

    /**
     * 특정 시간대의 경우 파싱을 제외한다.
     *
     * 예를 들어 시즌 마지막 날 KST 타임 시간 기준 23:50 ~ 23:55 는
     * 시즌식민지를 위해 정비하는 시간을 가질 수 있으므로, 이 시간에는 파싱을 제한한다.
     *
     * @return
     */
    public boolean ExceptTime() {

        return false;
    }

    public boolean isSetSeasonDate() {
        if (this.startDate == null || this.endDate == null) {
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        System.out.println("Hello World!");
    }
}