package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Setter
@Getter
@Data
public class InconsistencyReport {
    private Integer id;
    private String description;
    private LocalTime timeReported;
    private User reportedBy;
    private Driver driver;
}
