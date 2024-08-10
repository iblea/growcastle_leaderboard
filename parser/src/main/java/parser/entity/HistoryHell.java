package parser.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "History_player")
public class HistoryHell extends LeaderboardBaseEntity {

    public HistoryHell(LeaderboardBaseEntity entity) {
        super(entity);
    }

}