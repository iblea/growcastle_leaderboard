package parser.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "history_player")
public class HistoryPlayer extends HistoryBaseEntity {

    public HistoryPlayer() {
        super();
    }

    public HistoryPlayer(LeaderboardBaseEntity entity) {
        super(entity);
    }

    public HistoryPlayer(LeaderboardBaseEntity entity, String season, int minUnit) {
        super(entity, season, minUnit);
    }

    public HistoryPlayer(HistoryBaseEntity entity) {
        super(entity);
    }

}