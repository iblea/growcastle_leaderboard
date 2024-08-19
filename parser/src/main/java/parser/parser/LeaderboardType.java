package parser.parser;

public enum LeaderboardType {

    PLAYER(1, "players", "leaderboard_player", "history_player"),
    GUILD(2, "guilds", "leaderboard_guild", "history_guild"),
    HELL(3, "hell", "leaderboard_hell", "history_hell");

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
