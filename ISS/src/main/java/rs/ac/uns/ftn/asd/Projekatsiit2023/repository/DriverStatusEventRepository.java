package rs.ac.uns.ftn.asd.Projekatsiit2023.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.DriverStatusEvent;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DriverStatusEventRepository extends JpaRepository<DriverStatusEvent, Integer> {
    @Query("SELECT d FROM DriverStatusEvent d WHERE d.driver.id = :driverId AND d.timestamp >= :timestamp ORDER BY d.timestamp ASC")
    List<DriverStatusEvent> findByDriverIdAndTimestampAfter(@Param("driverId") Integer driverId, @Param("timestamp") LocalDateTime timestamp);

    @Query("SELECT d FROM DriverStatusEvent d WHERE d.driver.id = :driverId AND d.timestamp >= :startTime AND d.timestamp <= :endTime ORDER BY d.timestamp ASC")
    List<DriverStatusEvent> findByDriverIdAndTimestampBetween(
            @Param("driverId") Integer driverId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT d FROM DriverStatusEvent d WHERE d.driver.id = :driverId ORDER BY d.timestamp DESC LIMIT 1")
    DriverStatusEvent findLastEventByDriverId(@Param("driverId") Integer driverId);

    @Modifying
    @Query("DELETE FROM DriverStatusEvent d WHERE d.timestamp < :timestamp")
    void deleteOlderThan(@Param("timestamp") LocalDateTime timestamp);
}

