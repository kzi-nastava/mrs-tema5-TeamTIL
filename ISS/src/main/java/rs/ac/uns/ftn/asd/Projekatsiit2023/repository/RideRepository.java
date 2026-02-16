package rs.ac.uns.ftn.asd.Projekatsiit2023.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Driver;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Integer> {

    Optional<Ride> findById(Integer id);
    @Query("SELECT r FROM Ride r " +
            "JOIN FETCH r.driver d " +
            "JOIN FETCH d.vehicle v " +
            "WHERE r.id = :id")
    Optional<Ride> findByIdWithDriverAndVehicle(@Param("id") Integer id);

    List<Ride> findByPassengerId(Integer passengerId);

    List<Ride> findByDriverId(Integer driverId);

    List<Ride> findByRideStatus(RideStatus rideStatus);

    List<Ride> findByDriverIdAndRideStatusIn(Integer driverId, List<RideStatus> statuses);

    List<Ride> findByDriver_EmailAndRideStatusIn(String email, List<RideStatus> statuses);

    List<Ride> findByPassenger_EmailAndRideStatusIn(String email, List<RideStatus> statuses);

    List<Ride> findByRideStatusIn(List<RideStatus> statuses);

    @Query("SELECT r FROM Ride r WHERE r.id = :id AND r.rideStatus = 'IN_PROGRESS'")
    Optional<Ride> findActiveRideById(@Param("id") Integer id);

    @Query("SELECT r FROM Ride r WHERE r.driver = :driver AND r.rideStatus = :status")
    List<Ride> findByDriverAndRideStatus(@Param("driver") Driver driver, @Param("status") RideStatus status);
}
