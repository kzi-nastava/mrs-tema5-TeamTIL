package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideCancelResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideEstimationResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideStopResponseDTO;

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

    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<RideCancelResponseDTO> cancelRide(
            @PathVariable Long rideId,
            @RequestParam(required = false) String reason) {

        if (rideId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        RideCancelResponseDTO response = new RideCancelResponseDTO(
                rideId,
                "CANCELLED",
                reason != null ? reason : "User cancelled",
                "Ride cancelled successfully"
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{rideId}/stop")
    public ResponseEntity<RideStopResponseDTO> stopRide(
            @PathVariable Long rideId,
            @RequestParam(required = false) String location) {

        if (rideId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        double finalPrice = Math.random() * 2000 + 500; // 500-2500 din
        String duration = String.format("%d min", (int)(Math.random() * 30 + 10)); // 10-40 min

        RideStopResponseDTO response = new RideStopResponseDTO(
                rideId,
                "COMPLETED",
                location != null ? location : "Destination reached",
                Math.round(finalPrice * 100.0) / 100.0,
                duration,
                "Ride completed successfully"
        );

        return ResponseEntity.ok(response);
    }
}