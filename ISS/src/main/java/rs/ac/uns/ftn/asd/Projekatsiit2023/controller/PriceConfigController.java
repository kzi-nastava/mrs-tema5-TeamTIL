package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.PriceConfigDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.PriceConfigService;

@RestController
@RequestMapping("/api/price-config")
@Validated
public class PriceConfigController {
    @Autowired
    private PriceConfigService priceConfigService;

    @GetMapping("/{vehicleType}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<PriceConfigDTO> getPriceConfig(
            @PathVariable VehicleType vehicleType) {

        PriceConfigDTO config = priceConfigService.getPriceConfig(vehicleType);
        return ResponseEntity.ok(config);
    }

    @PutMapping("/{vehicleType}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<PriceConfigDTO> updatePriceConfig(
            @PathVariable VehicleType vehicleType,
            @RequestBody @Valid PriceConfigDTO dto) {

        PriceConfigDTO updated = priceConfigService.updatePriceConfig(vehicleType, dto);
        return ResponseEntity.ok(updated);
    }
}
