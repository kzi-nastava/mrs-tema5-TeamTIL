package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.VehicleStatusDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Vehicle;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.DriverRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final DriverRepository driverRepository;

    public List<VehicleStatusDTO> getAllVehicles() {
        return driverRepository.findAll()
                .stream()
                .filter(driver -> driver.getVehicle() != null)
                .map(driver -> {
                    Vehicle v = driver.getVehicle();
                    return new VehicleStatusDTO(
                            v.getModel(),
                            v.getType() != null ? v.getType().name() : "UNKNOWN",
                            v.getLicensePlate(),
                            driver.getIsActive(),
                            v.getCurrentLatitude(),
                            v.getCurrentLongitude()
                    );
                })
                .toList();
    }
}
