package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RegisteredUser extends User {
    private List<Route> favoriteRoutes;
}
