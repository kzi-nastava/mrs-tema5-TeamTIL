package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.RideHistoryDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.RideRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideCancelResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideEstimationResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideStopResponseDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @GetMapping("/admin/history")
    public ResponseEntity<List<RideHistoryDTO>> getAllRidesHistory(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String driverEmail,
            @RequestParam(required = false) String passengerEmail,
            @RequestParam(required = false) String startLocation,
            @RequestParam(required = false) String endLocation,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<RideHistoryDTO> ridesHistory = new ArrayList<>();
        List<String> allStatuses = Arrays.asList("COMPLETED", "CANCELLED", "IN_PROGRESS");

        for (int i = 1; i <= size * 3; i++) {
            int rideId = (page * size + i);

            String rideStatus = status != null ? status : allStatuses.get(rideId % 3);
            String passengerEmailValue = "passenger" + ((rideId % 5) + 1) + "@example.com";
            String driverEmailValue = "driver" + ((rideId % 3) + 1) + "@example.com";
            String startLoc = "Bulevar oslobođenja " + (rideId % 100);
            String endLoc = "Trg slobode " + (rideId % 50);
            Double price = Math.round((Math.random() * 2000 + 500) * 100.0) / 100.0;
            String createdAt = "2024-01-" + String.format("%02d", (rideId % 28) + 1) + "T12:00:00";

            RideHistoryDTO ride = new RideHistoryDTO(
                    rideId, passengerEmailValue, driverEmailValue,
                    startLoc, endLoc, rideStatus, price, createdAt
            );

            boolean passesFilter = true;

            if (driverEmail != null && !ride.getDriverEmail().contains(driverEmail)) {
                passesFilter = false;
            }
            if (passengerEmail != null && !ride.getPassengerEmail().contains(passengerEmail)) {
                passesFilter = false;
            }
            if (startLocation != null && !ride.getStartLocation().toLowerCase().contains(startLocation.toLowerCase())) {
                passesFilter = false;
            }
            if (endLocation != null && !ride.getEndLocation().toLowerCase().contains(endLocation.toLowerCase())) {
                passesFilter = false;
            }
            if (minPrice != null && ride.getPrice() < minPrice) {
                passesFilter = false;
            }
            if (maxPrice != null && ride.getPrice() > maxPrice) {
                passesFilter = false;
            }

            if (passesFilter && ridesHistory.size() < size) {
                ridesHistory.add(ride);
            }
        }

        return ResponseEntity.ok(ridesHistory);
    }

    // 2.4.1 Ordering a ride
    @PostMapping
    public ResponseEntity<RideHistoryDTO> createRide(@RequestBody RideRequestDTO request) {

        Double price = 150.0 + (Math.random() * 10 * 120);

        RideHistoryDTO response = new RideHistoryDTO(
                101, "me@example.com", "driver@example.com",
                request.getLocations().get(0), request.getLocations().get(request.getLocations().size()-1),
                "ACCEPTED", Math.round(price * 100.0) / 100.0, "2025-12-28T15:00:00"
        );
        return ResponseEntity.ok(response);
    }

    // 2.4.3 Ordering from your favorite routes
    @PostMapping("/favorites/{routeId}")
    public ResponseEntity<RideHistoryDTO> createRideFromFavorite(@PathVariable Long routeId) {
        RideHistoryDTO response = new RideHistoryDTO(
                102, "me@example.com", "driver@example.com",
                "Favorite Start", "Favorite End",
                "ACCEPTED", 500.0, "2025-12-28T15:30:00"
        );
        return ResponseEntity.ok(response);
    }

    // 2.6.1 The start of the ride
    @PutMapping("/{rideId}/start")
    public ResponseEntity<String> startRide(@PathVariable Long rideId) {
        return ResponseEntity.ok("Ride " + rideId + " has started.");
    }
}