package parser.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "History_player")
public class HistoryPlayer extends LeaderboardBaseEntity {

    public HistoryPlayer(LeaderboardBaseEntity entity) {
        super(entity);
    }

}