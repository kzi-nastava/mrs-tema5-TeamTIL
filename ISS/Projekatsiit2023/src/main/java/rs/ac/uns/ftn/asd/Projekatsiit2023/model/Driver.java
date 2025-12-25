package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Setter
@Getter
public class Driver extends User {
    private Boolean isActive;
    private Duration aciveHours;
    private Vehicle vehicle;
}
