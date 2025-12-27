package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class PanicNotification extends Notification {
    private Location location;
    private Boolean handled;
    private RegisteredUser registeredUser;
    private Driver driver;
}
