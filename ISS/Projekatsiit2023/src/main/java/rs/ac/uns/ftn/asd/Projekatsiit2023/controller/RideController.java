package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideEstimationResponseDTO;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @GetMapping("/estimate")
    public ResponseEntity<RideEstimationResponseDTO> getRideEstimation(
            @RequestParam String pickupAddress,
            @RequestParam String destinationAddress,
            @RequestParam(defaultValue = "STANDARD") String vehicleType) {

        double distance = Math.random() * 20 + 5; // 5-25 km
        double basePrice = distance * 120; // 120 din per km

        double multiplier = switch (vehicleType.toUpperCase()) {
            case "STANDARD" -> 1.0;
            case "LUXURY" -> 1.5;
            case "VAN" -> 1.3;
            default -> 1.0;
        };

        double finalPrice = basePrice * multiplier;
        String estimatedTime = String.format("%d min", (int)(distance * 2 + 5));

        RideEstimationResponseDTO response = new RideEstimationResponseDTO(
                estimatedTime,
                Math.round(distance * 100.0) / 100.0,
                Math.round(finalPrice * 100.0) / 100.0,
                vehicleType
        );

        return ResponseEntity.ok(response);
    }
}