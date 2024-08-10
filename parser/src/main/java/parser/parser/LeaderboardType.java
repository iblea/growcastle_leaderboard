package parser.parser;

public enum LeaderboardType {

    PLAYER(1, "players", "LEADERBOARD_PLAYER", "HISTORY_PLAYER"),
    GUILD(2, "guilds", "LEADERBOARD_GUILD", "HISTORY_GUILD"),
    HELL(3, "hell", "LEADERBOARD_HELL", "HISTORY_HELL");

    private String typename;
    private int type;
    private String realTimetableName;
    private String historyTableName;

    LeaderboardType(int type, String typename, String realTimetableName, String historyTableName) {
        this.typename = typename;
        this.type = type;
        this.realTimetableName = realTimetableName;
        this.historyTableName = historyTableName;
    }

    public String getTypename() {
        return this.typename;
    }

    public int getType() {
        return this.type;
    }

    public String getRealTimeTableName() {
        return this.realTimetableName;
    }

    public String getHistoryTableName() {
        return this.historyTableName;
    }

}
