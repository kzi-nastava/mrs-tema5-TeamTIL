package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.PriceConfigDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.PriceConfig;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PriceConfigRepository;

@Service
public class PriceConfigService {
    @Autowired
    private PriceConfigRepository priceConfigRepository;

    public PriceConfigDTO getPriceConfig(VehicleType vehicleType) {
        PriceConfig config = priceConfigRepository.findByVehicleType(vehicleType)
                .orElseThrow(() -> new RuntimeException("Price config not found for: " + vehicleType));

        return toDTO(config);
    }

    public PriceConfigDTO updatePriceConfig(VehicleType vehicleType, PriceConfigDTO dto) {
        PriceConfig config = priceConfigRepository.findByVehicleType(vehicleType)
                .orElseGet(() -> {
                    PriceConfig newConfig = new PriceConfig();
                    newConfig.setVehicleType(vehicleType);
                    return newConfig;
                });

        config.setBasePrice(dto.getBasePrice());
        config.setPricePerKm(dto.getPricePerKm());

        PriceConfig saved = priceConfigRepository.save(config);
        return toDTO(saved);
    }

    private PriceConfigDTO toDTO(PriceConfig config) {
        return new PriceConfigDTO(
                config.getVehicleType(),
                config.getBasePrice(),
                config.getPricePerKm()
        );
    }
}
