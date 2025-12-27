package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Setter
@Getter
@Data
public class Notification {
    private Integer id;
    private String message;
    private LocalTime timeSent;
    private Boolean isRead;
}
