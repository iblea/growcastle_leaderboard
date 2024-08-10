package parser.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "History_player")
public class HistoryGuild extends LeaderboardBaseEntity {

    public HistoryGuild(LeaderboardBaseEntity entity) {
        super(entity);
    }

}