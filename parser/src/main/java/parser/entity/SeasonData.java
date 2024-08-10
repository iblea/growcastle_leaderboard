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

    public SeasonData() {
        this.seasonDataPK = new SeasonDataPK();
    }

    public void setStartDate(LocalDateTime startDate) {
        this.seasonDataPK.setStartDate(startDate);
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

}