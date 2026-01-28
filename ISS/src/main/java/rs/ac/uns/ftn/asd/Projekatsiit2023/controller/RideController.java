package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.AssignedRideDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.DriverRideDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.RideHistoryDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.InconsistencyReportResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideCancelResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideEstimationResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideRatingResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideStopResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideTrackingDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Location;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.PriceConfig;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Route;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PriceConfigRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RideRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.LocationService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RideService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RouteService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private PriceConfigRepository priceConfigRepository;

    @Autowired
    private RouteService routeService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private RideService rideService;

    @PutMapping("/{rideId}/cancel")
    @Transactional
    public ResponseEntity<RideCancelResponseDTO> cancelRide(
            @PathVariable Integer rideId,
            @RequestBody(required = false) RideCancelRequestDTO request) {

        if (rideId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        // Pronađi vožnju iz baze
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        // Postavi razlog otkazivanja
        String reason = (request != null && request.getCancellationReason() != null)
                ? request.getCancellationReason()
                : "User cancelled";

        // Ažuriraj status i razlog
        ride.setRideStatus(RideStatus.CANCELED);
        ride.setCancellationReason(reason);

        // Sačuvaj promene u bazi
        rideRepository.save(ride);

        RideCancelResponseDTO response = new RideCancelResponseDTO(
                rideId,
                "CANCELLED",
                reason,
                "Ride cancelled successfully");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{rideId}/stop")
    @Transactional
    public ResponseEntity<RideStopResponseDTO> stopRide(
            @PathVariable Integer rideId,
            @RequestBody RideStopRequestDTO request) {

        if (rideId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        Route route = ride.getRoute();

        VehicleType vehicleType = ride.getDriver().getVehicle().getType();
        double finalPrice = rideService.calculateFinalPrice(
                vehicleType,
                ride.getStartLocation(),
                request.getActualEndLocation()
        );

        Location endLocation = request.getActualEndLocation();
        endLocation.setRoute(route);

        endLocation = locationService.findOrSaveLocation(endLocation, route);

        if (endLocation != null) {
            route.getLocations().add(endLocation);
        }
        routeService.save(route);

        ride.setRideStatus(RideStatus.FINISHED);
        ride.setEndLocation(endLocation);
        ride.setEndTime(request.getActualEndTime());
        ride.setTotalPrice(finalPrice);
        ride.setRoute(route);

        rideRepository.save(ride);

        RideStopResponseDTO response = new RideStopResponseDTO(
                rideId,
                "COMPLETED",
                endLocation.getAddress(),
                Math.round(finalPrice * 100.0) / 100.0,
                "duration",
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
                    startLoc, endLoc, rideStatus, price, createdAt);

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
    public ResponseEntity<?> createRide(@RequestBody RideRequestDTO request) {
        try {
            // Pozivamo servis da obradi logiku i sacuva u bazu
            RideHistoryDTO response = rideService.createNewRide(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating ride: " + e.getMessage());
        }
    }

    // 2.4.3 Ordering from your favorite routes
    @PostMapping("/favorites/{routeId}")
    public ResponseEntity<RideHistoryDTO> createRideFromFavorite(@PathVariable Long routeId) {
        RideHistoryDTO response = new RideHistoryDTO(
                102, "me@example.com", "driver@example.com",
                "Favorite Start", "Favorite End",
                "ACCEPTED", 500.0, "2025-12-28T15:30:00");
        return ResponseEntity.ok(response);
    }

    // 2.6.1 The start of the ride
    @PutMapping("/{rideId}/start")
    public ResponseEntity<String> startRide(@PathVariable Long rideId) {
        return ResponseEntity.ok("Ride " + rideId + " has started.");
    }

    // 2.6.2 Track ride location
    @GetMapping("/{rideId}/tracking")
    public ResponseEntity<RideTrackingDTO> trackRide(@PathVariable Long rideId) {

        if (rideId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        String[] locations = { "Main Street 1", "Bulevar Oslobođenja 5", "Trg Slobode 3" };
        String vehicleLocation = locations[(int) (Math.random() * locations.length)];

        // Procena vremena dolaska: 5-15 minuta
        int minutes = 5 + (int) (Math.random() * 11);
        String estimatedArrivalTime = minutes + " min";

        RideTrackingDTO response = new RideTrackingDTO(
                rideId,
                vehicleLocation,
                estimatedArrivalTime,
                "IN_PROGRESS");

        return ResponseEntity.ok(response);
    }

    // 2.6.2 Report inconsistency
    @PostMapping("/{rideId}/report")
    public ResponseEntity<InconsistencyReportResponseDTO> reportInconsistency(
            @PathVariable Long rideId,
            @RequestBody InconsistencyReportRequestDTO report) {

        InconsistencyReportResponseDTO response = new InconsistencyReportResponseDTO(
                rideId,
                report.getPassengerEmail(),
                report.getLocation() != null ? report.getLocation() : "Unknown location",
                report.getMessage(),
                "Report submitted successfully");

        return ResponseEntity.ok(response);
    }

    // 2.7 Complete the ride
//    @PutMapping("/{rideId}/complete")
//    public ResponseEntity<RideStopResponseDTO> completeRide(
//            @PathVariable Long rideId,
//            @RequestParam(required = false) String finalLocation) {
//
//        if (rideId <= 0) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        double finalPrice = Math.random() * 2000 + 500; // 500-2500 din
//        String duration = String.format("%d min", (int) (Math.random() * 30 + 10)); // 10-40 min
//
//        RideStopResponseDTO response = new RideStopResponseDTO(
//                rideId,
//                "COMPLETED",
//                finalLocation != null ? finalLocation : "Destination reached",
//                Math.round(finalPrice * 100.0) / 100.0,
//                duration,
//                "Ride completed and paid successfully");
//        return ResponseEntity.ok(response);
//    }

    // 2.8 Rate ride, driver and vehicle
    @PostMapping("/{rideId}/rate")
    public ResponseEntity<RideRatingResponseDTO> rateRide(
            @PathVariable Long rideId,
            @RequestBody RideRatingRequestDTO request) {

        // provera roka od 3 dana
        LocalDate rideDate = LocalDate.now().minusDays(2); // primer završene vožnje pre 2 dana
        boolean withinDeadline = ChronoUnit.DAYS.between(rideDate, LocalDate.now()) <= 3;

        if (!withinDeadline) {
            return ResponseEntity.badRequest().body(
                    new RideRatingResponseDTO(rideId, "UNRATED", "Deadline exceeded, rating not accepted"));
        }

        // uspešna ocena
        RideRatingResponseDTO response = new RideRatingResponseDTO(
                rideId,
                "RATED",
                "Rating submitted successfully: Driver=" + request.getDriverRating() +
                        ", Vehicle=" + request.getVehicleRating() +
                        ", Comment='" + request.getComment() + "'");

        return ResponseEntity.ok(response);
    }

    // 2.9.2 Driver's ride history
    @GetMapping("/driver/history")
    public ResponseEntity<List<DriverRideDTO>> getDriverRideHistory(
            @RequestParam String driverEmail,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {

        List<RideStatus> statuses = List.of(RideStatus.FINISHED, RideStatus.CANCELED);
        List<Ride> rides = rideRepository.findByDriver_EmailAndRideStatusIn(driverEmail, statuses);

        LocalDate from = (dateFrom != null) ? LocalDate.parse(dateFrom) : null;
        LocalDate to = (dateTo != null) ? LocalDate.parse(dateTo) : null;

        List<DriverRideDTO> result = rides.stream()
                .filter(r -> {
                    LocalDate rideDate = r.getStartTime().toLocalDate();
                    boolean afterFrom = (from == null) || !rideDate.isBefore(from);
                    boolean beforeTo = (to == null) || !rideDate.isAfter(to);
                    return afterFrom && beforeTo;
                })
                .sorted((r1, r2) -> r2.getStartTime().compareTo(r1.getStartTime()))
                .map(rideService::mapRideToDriverRideDTO)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<AssignedRideDTO>> getAssignedRides(
            @RequestParam String driverEmail,
            @RequestParam(required = false) String status
    ) {
        List<RideStatus> statuses;
        if (status != null && !status.isEmpty()) {
            statuses = Arrays.stream(status.split(","))
                    .map(String::trim)
                    .map(RideStatus::valueOf)
                    .toList();
        } else {
            statuses = List.of(RideStatus.IN_PROGRESS, RideStatus.REQUESTED);
        }

        List<Ride> rides = rideRepository.findByDriver_EmailAndRideStatusIn(driverEmail, statuses);

        List<AssignedRideDTO> result = rides.stream()
                .map(ride -> rideService.mapRideToDTO(ride, UserType.DRIVER))
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{email}")
    public List<AssignedRideDTO> getRidesForUser(
            @PathVariable String email,
            @RequestParam(required = false) String status
    ) {
        List<RideStatus> statuses;
        if (status != null && !status.isEmpty()) {
            statuses = Arrays.stream(status.split(","))
                    .map(String::trim)
                    .map(RideStatus::valueOf)
                    .toList();
        } else {
            statuses = List.of(RideStatus.IN_PROGRESS, RideStatus.REQUESTED);
        }

        List<Ride> rides = rideRepository.findByPassenger_EmailAndRideStatusIn(email, statuses);

        return rides.stream()
                .map(ride -> rideService.mapRideToDTO(ride, UserType.REGISTERED_USER))
                .toList();
    }

}