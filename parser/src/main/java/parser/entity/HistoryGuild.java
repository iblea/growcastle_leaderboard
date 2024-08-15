package parser.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "history_guild")
public class HistoryGuild extends HistoryBaseEntity {

    public HistoryGuild(LeaderboardBaseEntity entity) {
        super(entity);
    }

    public HistoryGuild(LeaderboardBaseEntity entity, String season, int minUnit) {
        super(entity, season, minUnit);
    }

    public HistoryGuild(HistoryBaseEntity entity) {
        super(entity);
    }

}