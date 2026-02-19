package rs.ac.uns.ftn.asd.Projekatsiit2023.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.PanicNotification;

import java.util.List;
import java.util.Optional;

public interface PanicNotificationRepository extends JpaRepository<PanicNotification, Integer> {
    boolean existsByRideId(Integer id);

    @Query("SELECT p FROM PanicNotification p ORDER BY p.timeSent DESC")
    List<PanicNotification> findAllOrderByTimeSentDesc();

    @Query("SELECT p FROM PanicNotification p WHERE p.handled = false ORDER BY p.timeSent DESC")
    List<PanicNotification> findUnhandledOrderByTimeSentDesc();

    @Query("SELECT p FROM PanicNotification p WHERE p.handled = ?1 ORDER BY p.timeSent DESC")
    List<PanicNotification> findByHandledOrderByTimeSentDesc(Boolean handled);

    Page<PanicNotification> findAll(Pageable pageable);
}


