package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.VehicleStatusDTO;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    
    // 2.1.1 Display info for unregistered users
    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleStatusDTO>> getActiveVehicles() {
        List<VehicleStatusDTO> vehicles = Arrays.asList(
                new VehicleStatusDTO("Car1", "STANDARD", true, "Location1"),
                new VehicleStatusDTO("Car2", "LUX", false, "Location2"));
        return ResponseEntity.ok(vehicles);
    }
}
