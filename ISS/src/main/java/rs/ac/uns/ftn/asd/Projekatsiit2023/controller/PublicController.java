package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.VehicleStatusDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.VehicleService;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {
    private final VehicleService vehicleService;

    // 2.1.1 Display info for unregistered users
    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleStatusDTO>> getActiveVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }
}
