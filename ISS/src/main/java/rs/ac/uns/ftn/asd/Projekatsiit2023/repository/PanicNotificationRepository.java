package rs.ac.uns.ftn.asd.Projekatsiit2023.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.PanicNotification;

import java.util.Optional;

public interface PanicNotificationRepository extends JpaRepository<PanicNotification, Integer> {
    boolean existsByRideId(Integer id);
}
