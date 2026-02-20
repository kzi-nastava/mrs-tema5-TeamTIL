package rs.ac.uns.ftn.asd.Projekatsiit2023.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Route;

public interface VehicleRepository extends JpaRepository<Route, Integer> {
    @Modifying
    @Transactional
    @Query("UPDATE Vehicle v SET v.currentLatitude = :lat, v.currentLongitude = :lng WHERE v.id = :id")
    void updatePosition(@Param("id") Integer id, @Param("lat") Double lat, @Param("lng") Double lng);
}
