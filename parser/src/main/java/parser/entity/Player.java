package parser.entity;

public class Player {

    private int rank;
    private String name;
    private int score;
    private long parseTime;

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