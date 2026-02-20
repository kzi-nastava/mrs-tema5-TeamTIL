package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.AssignedRideDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.DriverRideDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.InconsistencyReportResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideCancelResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideRatingResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideStopResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideTrackingDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.InconsistencyReportService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.LocationService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RideService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.RouteService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideStatsResponseDTO;

import java.security.Principal;
import java.util.Map;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/rides")
@Validated
public class RideController {
    private static final Logger logger = LoggerFactory.getLogger(RideController.class);

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

        // Validacija null vrednosti
        if (request.getActualEndLocation() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getActualEndTime() == null) {
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
    @PreAuthorize("hasRole('REGISTERED_USER')")
    public ResponseEntity<?> createRide(
            @RequestBody RideRequestDTO request,
            Principal principal) {

        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }

            String userEmail = principal.getName();
            RideCreatedResponseDTO response = rideService.createNewRide(request, userEmail);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Error creating ride: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    // 2.6.1 The start of the ride
    @PutMapping("/{rideId}/start")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<?> startRide(@PathVariable Integer rideId) {
        try {
            RideStartResponseDTO response = rideService.startRide(rideId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2.6.2 Track ride location
    @GetMapping("/{rideId}/tracking")
    @PreAuthorize("hasAnyRole('REGISTERED_USER', 'DRIVER', 'ADMINISTRATOR')")
    public ResponseEntity<RideTrackingDTO> trackRide(@PathVariable Integer rideId, Principal principal) {
        if (rideId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            return ResponseEntity.notFound().build();
        }

        // Vožnja mora biti IN_PROGRESS za obične korisnike
        if (!RideStatus.IN_PROGRESS.equals(ride.getRideStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Admin ima pristup svemu
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMINISTRATOR"));

        // Provera da li je korisnik povezan sa vožnjom (vozač, putnik ili ulinkovani putnik)
        if (!isAdmin) {

            String email = principal.getName();
            boolean isDriver = ride.getDriver() != null
                    && ride.getDriver().getEmail().equals(email);
            boolean isPassenger = ride.getPassenger() != null
                    && ride.getPassenger().getEmail().equals(email);
            boolean isCoPassenger = ride.getCoPassengers() != null
                    && ride.getCoPassengers().stream().anyMatch(p -> p.getEmail().equals(email));

            if (!isDriver && !isPassenger && !isCoPassenger) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        RideTrackingDTO response = rideService.getRideTracking(rideId);
        return ResponseEntity.ok(response);
    }

    // 2.6.2 Report inconsistency
    @PostMapping("/{rideId}/report")
    @PreAuthorize("hasRole('REGISTERED_USER')")
    public ResponseEntity<InconsistencyReportResponseDTO> reportInconsistency(
            @PathVariable Integer rideId,
            @Valid @RequestBody InconsistencyReportRequestDTO reportDTO) {
        if (rideId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        inconsistencyReportService.saveReportWithAttachment(
                rideId,
                reportDTO.getPassengerEmail(),
                reportDTO.getDescription(),
                reportDTO.getAttachmentBase64()
        );

        InconsistencyReportResponseDTO response = new InconsistencyReportResponseDTO(rideId, "Report submitted successfully");

        return ResponseEntity.ok(new InconsistencyReportResponseDTO(rideId, "Report submitted successfully"));
    }

    // 2.7 Complete the ride
    @PutMapping("/{rideId}/end")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideEndResponseDTO> endRide(
            @PathVariable Integer rideId,
            @Valid @RequestBody RideEndRequestDTO request) {
        if (rideId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        RideEndResponseDTO response = rideService.endRideAndNotify(rideId, request.getActualEndLocation());

        return ResponseEntity.ok(response);
    }

    // 2.8 Rate ride, driver and vehicle
    @PostMapping("/{rideId}/rate")
    @PreAuthorize("hasRole('REGISTERED_USER')")
    public ResponseEntity<RideRatingResponseDTO> rateRide(
            @PathVariable Integer rideId,
            @Valid @RequestBody RideRatingRequestDTO request) {
        if (rideId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return rideService.rateRide(rideId, request);
    }

    // 2.9.2 Driver's ride history
    @GetMapping("/driver/{driverEmail}/history")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<DriverRideDTO>> getDriverRideHistory(
            @PathVariable String driverEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {
        List<DriverRideDTO> result = rideService.getDriverRideHistory(driverEmail, dateFrom, dateTo);
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

    // statistika za korisnika
    @GetMapping("/stats/user/{email}")
    @PreAuthorize("hasAnyRole('REGISTERED_USER', 'ADMINISTRATOR')")
    public ResponseEntity<RideStatsResponseDTO> getUserStats(
            @PathVariable String email,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {

        LocalDateTime from = dateFrom != null ? LocalDateTime.parse(dateFrom) : null;
        LocalDateTime to   = dateTo   != null ? LocalDateTime.parse(dateTo)   : null;

        return ResponseEntity.ok(rideService.getUserStats(email, from, to));
    }

    // statistika za vozaca
    @GetMapping("/stats/driver/{email}")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMINISTRATOR')")
    public ResponseEntity<RideStatsResponseDTO> getDriverStats(
            @PathVariable String email,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {

        LocalDateTime from = dateFrom != null ? LocalDateTime.parse(dateFrom) : null;
        LocalDateTime to   = dateTo   != null ? LocalDateTime.parse(dateTo)   : null;

        return ResponseEntity.ok(rideService.getDriverStats(email, from, to));
    }

    // statistika za admina, svi ili jedna osoba
    @GetMapping("/stats/admin")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<RideStatsResponseDTO> getAdminStats(
            @RequestParam(defaultValue = "DRIVER") String role,
            @RequestParam(required = false) String filterEmail,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {

        LocalDateTime from = dateFrom != null ? LocalDateTime.parse(dateFrom) : null;
        LocalDateTime to   = dateTo   != null ? LocalDateTime.parse(dateTo)   : null;

        return ResponseEntity.ok(rideService.getAdminStats(from, to, role, filterEmail));
    }

    // Endpoint za pregled svih aktivnih vožnji (za admina)
    @GetMapping("/admin/active")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<ActiveRideAdminDTO>> getActiveRidesForAdmin() {
        List<ActiveRideAdminDTO> activeRides = rideService.getActiveRidesForAdmin();
        return ResponseEntity.ok(activeRides);
    }
}

