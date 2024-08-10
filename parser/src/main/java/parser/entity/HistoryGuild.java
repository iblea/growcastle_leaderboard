package parser.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "History_guild")
public class HistoryGuild extends LeaderboardBaseEntity {

    public HistoryGuild(LeaderboardBaseEntity entity) {
        super(entity);
    }

}