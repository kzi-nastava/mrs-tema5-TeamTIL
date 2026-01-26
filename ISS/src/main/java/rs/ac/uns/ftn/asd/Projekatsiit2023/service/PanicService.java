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
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.RegisteredUser;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Driver;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PanicNotificationRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RideRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.LocationRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RegisteredUserRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.DriverRepository;

import java.time.LocalTime;

@Service
public class PanicService {

    private final PanicNotificationRepository panicNotificationRepository;
    private final RideRepository rideRepository;
    private final LocationRepository locationRepository;
    private final RegisteredUserRepository registeredUserRepository;
    private final DriverRepository driverRepository;

    @Autowired
    public PanicService(
            PanicNotificationRepository panicNotificationRepository,
            RideRepository rideRepository,
            LocationRepository locationRepository,
            RegisteredUserRepository registeredUserRepository,
            DriverRepository driverRepository
    ) {
        this.panicNotificationRepository = panicNotificationRepository;
        this.rideRepository = rideRepository;
        this.locationRepository = locationRepository;
        this.registeredUserRepository = registeredUserRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public PanicResponseDTO createPanic(PanicRequestDTO request) {
        PanicNotification panic = new PanicNotification();

        Ride ride = rideRepository.findById(request.getRideId()).orElse(null);
        Location location = locationRepository.findById(request.getLocationId()).orElse(null);
        RegisteredUser registeredUser = registeredUserRepository.findById(request.getRegisteredUserId()).orElse(null);
        Driver driver = driverRepository.findById(request.getDriverId()).orElse(null);

        panic.setRide(ride);
        panic.setLocation(location);
        panic.setRegisteredUser(registeredUser);
        panic.setDriver(driver);
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

        return getPanicResponseDTO(saved);
    }

    private static PanicResponseDTO getPanicResponseDTO(PanicNotification saved) {
        PanicResponseDTO response = new PanicResponseDTO();
        response.setId(saved.getId());
        response.setRideId(saved.getRide() != null ? saved.getRide().getId() : null);
        response.setLocationId(saved.getLocation() != null ? saved.getLocation().getId() : null);
        response.setRegisteredUserId(saved.getRegisteredUser() != null ? saved.getRegisteredUser().getId() : null);
        response.setDriverId(saved.getDriver() != null ? saved.getDriver().getId() : null);
        response.setHandled(saved.getHandled());
        response.setTimestamp(saved.getTimeSent());
        response.setReportedBy(saved.getReportedBy());
        return response;
    }
}
