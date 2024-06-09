package parser.entity;

public class Leaderboard {

    private int rank;
    private String name;
    private int score;
    private long parseTime;

    public Leaderboard() {
        this.rank = -1;
        this.name = "";
        this.score = -1;
        this.parseTime = -1;
        // this.parseTime = System.currentTimeMillis() / 1000;
    }

    public Leaderboard(int rank, String name, int score)
    {
        this.rank = rank;
        this.name = name;
        this.score = score;
        this.parseTime = -1;
    }

    public Leaderboard(int rank, String name, int score, int parseTime)
    {
        this.rank = rank;
        this.name = name;
        this.score = score;
        this.parseTime = parseTime;
    }

    public int getRank() {
        return this.rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getParseTime() {
        return this.parseTime;
    }

    public void setParseTime(long parseTime) {
        this.parseTime = parseTime;
    }
}