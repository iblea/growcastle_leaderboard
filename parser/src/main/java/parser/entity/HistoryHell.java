package parser.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "history_hell")
public class HistoryHell extends HistoryBaseEntity {

    public HistoryHell() {
        super();
    }

    public HistoryHell(LeaderboardBaseEntity entity) {
        super(entity);
    }

    public HistoryHell(LeaderboardBaseEntity entity, String season, int minUnit) {
        super(entity, season, minUnit);
    }

    public HistoryHell(HistoryBaseEntity entity) {
        super(entity);
    }

}