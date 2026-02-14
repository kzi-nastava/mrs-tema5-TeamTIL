package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.AssignedRideDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.DriverRideDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.RideHistoryDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.InconsistencyReportResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideCancelResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideRatingResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideStopResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideTrackingDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.InconsistencyReportService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.LocationService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RideService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RouteService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAnyRole;

@RestController
@RequestMapping("/api/rides")
@Validated
public class RideController {

    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private PriceConfigRepository priceConfigRepository;

    @Autowired
    private RideRatingRepository rideRatingRepository;

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @Autowired
    private RouteService routeService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private RideService rideService;
    @Autowired
    private InconsistencyReportService inconsistencyReportService;

    @PutMapping("/{rideId}/cancel")
    @Transactional
    @PreAuthorize("hasAnyRole('REGISTERED_USER', 'DRIVER')")
    public ResponseEntity<RideCancelResponseDTO> cancelRide(
            @PathVariable Integer rideId,
            @RequestBody(required = false) RideCancelRequestDTO request) {

        if (rideId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        String reason = (request != null && request.getCancellationReason() != null)
                ? request.getCancellationReason()
                : null;

        try {
            RideCancelResponseDTO response = rideService.cancelRide(rideId, reason);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new RideCancelResponseDTO(
                            rideId,
                            "CANCEL_FAILED",
                            reason != null ? reason : "",
                            e.getMessage(),
                            null
                    )
            );
        }
    }

    @PutMapping("/{rideId}/stop")
    @Transactional
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideStopResponseDTO> stopRide(
            @PathVariable Integer rideId,
            @Valid @RequestBody RideStopRequestDTO request) {

        if (rideId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            RideStopResponseDTO response = rideService.stopRide(
                    rideId,
                    request.getActualEndLocation(),
                    request.getActualEndTime()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new RideStopResponseDTO(
                            rideId,
                            "STOP_FAILED",
                            request.getActualEndLocation() != null ? request.getActualEndLocation().getAddress() : "",
                            null,
                            null,
                            e.getMessage()
                    )
            );
        }
    }

    @GetMapping("/admin/history")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<RideHistoryResponseDTO>> getAllRidesHistory(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        LocalDateTime dateFromParsed = dateFrom != null ? LocalDateTime.parse(dateFrom) : null;
        LocalDateTime dateToParsed = dateTo != null ? LocalDateTime.parse(dateTo) : null;

        List<RideHistoryResponseDTO> ridesHistory = rideService.getAdminRideHistory(dateFromParsed, dateToParsed, sortBy, sortDirection);

        return ResponseEntity.ok(ridesHistory);
    }

    @GetMapping("/{passengerEmail}/history")
    @PreAuthorize("hasRole('REGISTERED_USER')")
    public ResponseEntity<List<RideHistoryResponseDTO>> getUserRidesHistory(
            @PathVariable String passengerEmail,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        LocalDateTime dateFromParsed = dateFrom != null ? LocalDateTime.parse(dateFrom) : null;
        LocalDateTime dateToParsed = dateTo != null ? LocalDateTime.parse(dateTo) : null;

        List<RideHistoryResponseDTO> ridesHistory = rideService.getUserRideHistory(passengerEmail, dateFromParsed, dateToParsed, sortBy, sortDirection);

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
    @PreAuthorize("hasRole('REGISTERED_USER')")
    public ResponseEntity<RideTrackingDTO> trackRide(@PathVariable Integer rideId) {

        if (rideId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));

        RideTrackingDTO response = new RideTrackingDTO(
                ride.getStartLocation().getAddress(),
                ride.getStartLocation().getLatitude(),
                ride.getStartLocation().getLongitude(),
                ride.getEndLocation().getAddress(),
                ride.getEndLocation().getLatitude(),
                ride.getEndLocation().getLongitude(),
                ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName(),
                ride.getDriver().getPhoneNumber(),
                ride.getDriver().getVehicle().getType().toString(),
                ride.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        );

        return ResponseEntity.ok(response);
    }

    // 2.6.2 Report inconsistency
    @PostMapping("/{rideId}/report")
    @PreAuthorize("hasRole('REGISTERED_USER')")
    public ResponseEntity<InconsistencyReportResponseDTO> reportInconsistency(
            @PathVariable Integer rideId,
            @RequestBody InconsistencyReportRequestDTO reportDTO) {

        InconsistencyReport report = inconsistencyReportService.saveReportWithAttachment(
                rideId,
                reportDTO.getPassengerEmail(),
                reportDTO.getDescription(),
                reportDTO.getAttachmentBase64()
        );

        InconsistencyReportResponseDTO response = new InconsistencyReportResponseDTO(
                rideId,
                "Inconsistency reported successfully"
        );

        return ResponseEntity.ok(response);
    }

    // 2.7 Complete the ride
    @PutMapping("/{rideId}/end")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideStopResponseDTO> endRide(
            @PathVariable Integer rideId,
            @RequestBody RideEndRequestDTO request) {

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
        ride.setEndTime(request.getActualEndTime() != null ? request.getActualEndTime() : LocalDateTime.now());
        ride.setTotalPrice(finalPrice);
        ride.setRoute(route);

        rideRepository.save(ride);

        long durationMinutes = ChronoUnit.MINUTES.between(ride.getStartTime(), ride.getEndTime());

        if (endLocation != null) {
            RideStopResponseDTO response = new RideStopResponseDTO(
                    rideId,
                    "COMPLETED",
                    endLocation.getAddress(),
                    Math.round(finalPrice * 100.0) / 100.0,
                    durationMinutes + " min",
                    "Ride completed successfully"
            );

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    // 2.8 Rate ride, driver and vehicle
    @PostMapping("/{rideId}/rate")
    @PreAuthorize("hasRole('REGISTERED_USER')")
    public ResponseEntity<RideRatingResponseDTO> rateRide(
            @PathVariable Integer rideId,
            @RequestBody RideRatingRequestDTO request,
            @AuthenticationPrincipal RegisteredUser rater) {
        Ride ride = rideRepository.findById(Math.toIntExact(rideId))
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getEndTime().isBefore(LocalDateTime.now().minusDays(3))) {
            return ResponseEntity.badRequest().body(
                    new RideRatingResponseDTO(rideId, "UNRATED", "Deadline exceeded, rating not accepted"));
        }

        Rating rating = new Rating();
        rating.setDriverRating(request.getDriverRating().doubleValue());
        rating.setVehicleRating(request.getVehicleRating().doubleValue());
        rating.setRatedDriver(ride.getDriver());
        rating.setRater(rater);
        rating.setRide(ride);
        rating.setComment(request.getComment());
        rating.setCreatedAt(LocalDateTime.now());
        rideRatingRepository.save(rating);
        RideRatingResponseDTO response = new RideRatingResponseDTO(
                rideId,
                "RATED",
                "Rating submitted successfully: Driver=" + request.getDriverRating() +
                        ", Vehicle=" + request.getVehicleRating() +
                        ", Comment='" + request.getComment() + "'"
        );

        return ResponseEntity.ok(response);
    }

    // 2.9.2 Driver's ride history
    @GetMapping("/driver/{driverEmail}/history")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<DriverRideDTO>> getDriverRideHistory(
            @PathVariable String driverEmail,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        LocalDateTime dateFromParsed = dateFrom != null ? LocalDateTime.parse(dateFrom) : null;
        LocalDateTime dateToParsed = dateTo != null ? LocalDateTime.parse(dateTo) : null;

        List<DriverRideDTO> result = rideService.getDriverRideHistory(driverEmail, dateFromParsed, dateToParsed);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasRole('DRIVER')")
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
    @PreAuthorize("hasRole('REGISTERED_USER') and #email == authentication.name")
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

    @GetMapping("/{rideId}/details")
    @PreAuthorize("hasAnyRole('REGISTERED_USER', 'DRIVER', 'ADMINISTRATOR')")
    public ResponseEntity<RideDetailsResponseDTO> getRideDetails(@PathVariable Integer rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        RideDetailsResponseDTO response = rideService.mapRideToDetailsDTO(ride);
        return ResponseEntity.ok(response);
    }
}

