package parser.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "history_guild")
public class HistoryGuild extends HistoryBaseEntity {

    public HistoryGuild() {
        super();
    }

    public HistoryGuild(LeaderboardBaseEntity entity) {
        super(entity);
    }

    public HistoryGuild(LeaderboardBaseEntity entity, String season, int minUnit) {
        super(entity, season, minUnit);
    }

    public HistoryGuild(LeaderboardBaseEntity entity, String season, int minUnit, LocalDateTime time) {
        super(entity, season, minUnit);
        this.setParseTime(time);
    }

    public HistoryGuild(HistoryBaseEntity entity) {
        super(entity);
    }

}