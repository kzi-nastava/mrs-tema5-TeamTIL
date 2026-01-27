package rs.ac.uns.ftn.asd.Projekatsiit2023.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Integer> {

    Optional<Ride> findById(Integer id);

    List<Ride> findByPassengerId(Integer passengerId);

    List<Ride> findByDriverId(Integer driverId);

    List<Ride> findByRideStatus(RideStatus rideStatus);

    List<Ride> findByDriverIdAndRideStatusIn(Integer driverId, List<RideStatus> statuses);

    List<Ride> findByDriver_EmailAndRideStatusIn(String email, List<RideStatus> statuses);

    List<Ride> findByPassenger_EmailAndRideStatusIn(String email, List<RideStatus> statuses);
}
