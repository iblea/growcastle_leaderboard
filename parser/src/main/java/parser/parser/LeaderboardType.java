package parser.parser;

public enum LeaderboardType {

    PLAYER("players", 1),
    GUILD("guilds", 2),
    HELL("hell", 3);

    private String typename;
    private int type;

    LeaderboardType(String typename, int type) {
        this.typename = typename;
        this.type = type;
    }

    public String getTypename() {
        return this.typename;
    }

    public int getType() {
        return this.type;
    }
}
