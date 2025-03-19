package parser.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
class SeasonDataPK implements Serializable {
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    public SeasonDataPK() {
        this.startDate = null;
        this.endDate = null;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final SeasonDataPK seasonData = (SeasonDataPK)obj;
        if (this.getStartDate() != seasonData.getStartDate()) {
            return false;
        }

        if (this.getEndDate() != seasonData.getEndDate()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.startDate,
            this.endDate
        );
    }
}


@Entity
@Table(name = "SeasonData")
public class SeasonData {

    @Id
    private SeasonDataPK seasonDataPK;

    @Column (name = "season_name")
    private String seasonName;

    public SeasonData() {
        this.seasonDataPK = new SeasonDataPK();
        this.seasonName = "";
    }

    public SeasonData(SeasonData seasonData) {
        this.seasonDataPK = new SeasonDataPK();
        this.seasonDataPK.setStartDate(seasonData.getStartDate());
        this.seasonDataPK.setEndDate(seasonData.getEndDate());
        this.seasonName = seasonData.getSeasonName();
    }

    public boolean isNull() {
        if (this.seasonDataPK.getStartDate() == null) {
            return true;
        }

        if (this.seasonDataPK.getEndDate() == null) {
            return true;
        }
        return false;
    }

    public boolean isNotNull() {
        if (this.isNull()) {
            return false;
        }
        return true;
    }

    public void setNull() {
        this.seasonDataPK.setStartDate(null);
        this.seasonDataPK.setEndDate(null);
    }

    public void setStartDate(LocalDateTime startDate) {
        this.seasonDataPK.setStartDate(startDate);
        this.seasonName = getSeasonNameWithTime(startDate);
    }

    public LocalDateTime getStartDate() {
        return this.seasonDataPK.getStartDate();
    }

    public void setEndDate(LocalDateTime endDate) {
        this.seasonDataPK.setEndDate(endDate);
    }

    public LocalDateTime getEndDate() {
        return this.seasonDataPK.getEndDate();
    }

    public void setSeasonName(String seasonName) {
        this.seasonName = seasonName;
    }

    public String getSeasonName() {
        return this.seasonName;
    }

    private String getSeasonNameWithTime(LocalDateTime timeobj) {
        // yyyy-mm-dd
        return timeobj.toString().substring(0, 10);
    }

}