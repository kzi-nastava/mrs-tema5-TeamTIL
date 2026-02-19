package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.PanicRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.PanicResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.PanicNotification;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Location;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PanicNotificationRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RideRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.LocationRepository;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PanicService {

    private final PanicNotificationRepository panicNotificationRepository;
    private final RideRepository rideRepository;
    private final LocationRepository locationRepository;
    private final NotificationService notificationService;

    @Autowired
    public PanicService(
            PanicNotificationRepository panicNotificationRepository,
            RideRepository rideRepository,
            LocationRepository locationRepository,
            NotificationService notificationService
    ) {
        this.panicNotificationRepository = panicNotificationRepository;
        this.rideRepository = rideRepository;
        this.locationRepository = locationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public PanicResponseDTO createPanic(PanicRequestDTO request) {
        PanicNotification panic = new PanicNotification();

        Ride ride = rideRepository.findById(request.getRideId()).orElse(null);
        Location location = locationRepository.findById(request.getLocationId()).orElse(null);

        panic.setRide(ride);
        panic.setLocation(location);
        assert ride != null;
        panic.setRegisteredUser(ride.getPassenger());
        panic.setDriver(ride.getDriver());
        panic.setHandled(false);
        panic.setIsRead(false);
        panic.setTimeSent(LocalTime.now());
        panic.setMessage("Panic alert!");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals("ROLE_DRIVER")) {
                    panic.setReportedBy(UserType.DRIVER);
                } else if (authority.getAuthority().equals("ROLE_REGISTERED_USER")) {
                    panic.setReportedBy(UserType.REGISTERED_USER);
                }
            }
        }

        PanicNotification saved = panicNotificationRepository.save(panic);

        // Send notification to all admins
        String reportedBy = saved.getReportedBy() != null ? saved.getReportedBy().toString() : "Unknown";
        String locationAddress = location != null ? location.getAddress() : "Unknown location";
        notificationService.sendPanicNotificationToAdmins(
                saved.getId(),
                reportedBy,
                locationAddress,
                ride != null ? ride.getId() : null
        );

        return getPanicResponseDTO(saved);
    }

    private PanicResponseDTO getPanicResponseDTO(PanicNotification saved) {
        // Use the same DateTimeFormatter as in RideService
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        PanicResponseDTO response = new PanicResponseDTO();
        response.setId(saved.getId());
        response.setRideId(saved.getRide() != null ? saved.getRide().getId() : null);
        response.setLocationId(saved.getLocation() != null ? saved.getLocation().getId() : null);
        response.setRegisteredUserId(saved.getRegisteredUser() != null ? saved.getRegisteredUser().getId() : null);
        response.setDriverId(saved.getDriver() != null ? saved.getDriver().getId() : null);
        response.setHandled(saved.getHandled() != null ? saved.getHandled() : false);

        // Format LocalTime to String using the same approach as RideService
        response.setTimestamp(saved.getTimeSent() != null ? saved.getTimeSent().format(formatter) : null);
        response.setReportedBy(saved.getReportedBy());
        
        // Set location details with null safety
        if (saved.getLocation() != null) {
            response.setLocationAddress(saved.getLocation().getAddress());
            response.setLatitude(saved.getLocation().getLatitude());
            response.setLongitude(saved.getLocation().getLongitude());
        } else {
            response.setLocationAddress("Unknown Location");
            response.setLatitude(0.0);
            response.setLongitude(0.0);
        }

        // Set vehicle details if driver exists
        if (saved.getDriver() != null && saved.getDriver().getVehicle() != null) {
            response.setVehicleName(saved.getDriver().getVehicle().getModel());
            response.setVehicleLicensePlate(saved.getDriver().getVehicle().getLicensePlate());
        } else {
            response.setVehicleName(null);
            response.setVehicleLicensePlate(null);
        }

        return response;
    }

    @Transactional
    public List<PanicResponseDTO> getAllPanics() {
        return panicNotificationRepository.findAllOrderByTimeSentDesc()
                .stream()
                .map(this::getPanicResponseDTO)
                .toList();
    }

    @Transactional
    public List<PanicResponseDTO> getUnhandledPanics() {
        return panicNotificationRepository.findUnhandledOrderByTimeSentDesc()
                .stream()
                .map(this::getPanicResponseDTO)
                .toList();
    }

    @Transactional
    public List<PanicResponseDTO> getPanicsByHandledStatus(Boolean handled) {
        return panicNotificationRepository.findByHandledOrderByTimeSentDesc(handled)
                .stream()
                .map(this::getPanicResponseDTO)
                .toList();
    }

    @Transactional
    public PanicResponseDTO getPanicById(Integer panicId) {
        return panicNotificationRepository.findById(panicId)
                .map(this::getPanicResponseDTO)
                .orElse(null);
    }

    @Transactional
    public PanicResponseDTO markPanicAsHandled(Integer panicId) {
        return panicNotificationRepository.findById(panicId)
                .map(panic -> {
                    panic.setHandled(true);
                    PanicNotification updated = panicNotificationRepository.save(panic);
                    return getPanicResponseDTO(updated);
                })
                .orElse(null);
    }
}
