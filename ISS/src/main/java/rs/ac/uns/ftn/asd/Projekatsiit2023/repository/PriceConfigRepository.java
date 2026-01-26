package rs.ac.uns.ftn.asd.Projekatsiit2023.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.PriceConfig;

import java.util.Optional;

public interface PriceConfigRepository extends JpaRepository<PriceConfig, Integer> {
    Optional<PriceConfig> findByVehicleType(VehicleType vehicleType);
}
