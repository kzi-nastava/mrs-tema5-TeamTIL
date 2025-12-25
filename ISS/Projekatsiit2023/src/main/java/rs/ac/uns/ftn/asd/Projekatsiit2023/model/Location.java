package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class Location {
    private Integer id;
    private Double latitude;
    private Double longitude;
    private String address;
}
