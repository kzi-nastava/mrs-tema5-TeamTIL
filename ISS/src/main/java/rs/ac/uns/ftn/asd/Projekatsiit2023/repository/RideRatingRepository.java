package rs.ac.uns.ftn.asd.Projekatsiit2023.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Rating;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;

import java.util.Optional;

public interface RideRatingRepository extends JpaRepository<Rating, Integer> {
    Optional<Rating> findByRide(Ride ride);
}
