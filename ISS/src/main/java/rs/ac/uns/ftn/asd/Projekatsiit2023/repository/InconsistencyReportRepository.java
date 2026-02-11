package rs.ac.uns.ftn.asd.Projekatsiit2023.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.InconsistencyReport;

import java.util.List;

@Repository
public interface InconsistencyReportRepository extends JpaRepository<InconsistencyReport, Integer> {
    List<InconsistencyReport> findAllByRideId(Integer rideId);
    List<InconsistencyReport> findAllByReportedById(Integer userId);
}
