package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.DriverRegistrationRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.DriverStatusEventType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Driver;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Vehicle;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.DriverStatusEvent;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.DriverRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.DriverStatusEventRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RideRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.DriverResponseDTO;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.UUID;
import java.util.List;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.ActivationRequestDTO;

@Service
@RequiredArgsConstructor
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    private final DriverRepository driverRepository;
    private final DriverStatusEventRepository driverStatusEventRepository;
    private final RideRepository rideRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;

    @Transactional
    public void registerDriver(DriverRegistrationRequestDTO dto) {
        Driver driver = new Driver();
        driver.setFirstName(dto.getFirstName());
        driver.setLastName(dto.getLastName());
        driver.setEmail(dto.getEmail());
        driver.setAddress(dto.getAddress());
        driver.setPhoneNumber(dto.getPhoneNumber());
        driver.setUserType(UserType.DRIVER);
        driver.setIsBlocked(false);
        driver.setIsActive(false); // Nalog nije aktivan dok ne postavi sifru

        // Generisemo token
        String token = UUID.randomUUID().toString();
        driver.setActivationToken(token);
        driver.setActivationTokenExpiration(LocalDateTime.now().plusHours(24)); // Vazi 24h

        // Postavljamo neku random sifru privremeno (bitno da nije null i da niko ne zna)
        driver.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        if (dto.getProfilePictureUrl() != null && !dto.getProfilePictureUrl().isEmpty()) {
            String publicId = "driver_" + dto.getEmail().replace("@", "_").replace(".", "_");
            String imageUrl = cloudinaryService.uploadBase64Image(dto.getProfilePictureUrl(), publicId);
            driver.setProfilePictureUrl(imageUrl);
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setModel(dto.getVehicleModel());
        vehicle.setType(dto.getVehicleType());
        vehicle.setLicensePlate(dto.getLicensePlate());
        vehicle.setCapacity(dto.getPassengerCapacity());
        vehicle.setBabyFriendly(dto.getBabyFriendly());
        vehicle.setPetFriendly(dto.getPetFriendly());
        vehicle.setCurrentLatitude(45.25 + Math.random() * 0.04);
        vehicle.setCurrentLongitude(19.81 + Math.random() * 0.04);

        driver.assignVehicle(vehicle);
        driverRepository.save(driver);

        // saljemo mejl
        emailService.sendActivationEmail(driver.getEmail(), token);
    }

    // metoda za aktivaciju
    @Transactional
    public void activateDriverAccount(ActivationRequestDTO request) {
        Driver driver = driverRepository.findByActivationToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired activation token"));

        if (driver.getActivationTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Activation token has expired");
        }

        // postavi novu sifru
        driver.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // aktiviraj nalog
        driver.setIsActive(true);

        // obrisi token da se ne moze iskoristiti ponovo
        driver.setActivationToken(null);
        driver.setActivationTokenExpiration(null);

        driverRepository.save(driver);
    }

    @Transactional
    public DriverResponseDTO getDriverProfile(String email) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        Vehicle v = driver.getVehicle();

        return new DriverResponseDTO(
                driver.getId().intValue(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getEmail(),
                driver.getPhoneNumber(),
                driver.getAddress(),
                driver.getProfilePictureUrl(),
                v != null ? v.getModel() : "",
                (v != null && v.getType() != null) ? v.getType().name() : "", // Enum u String
                v != null ? v.getLicensePlate() : "",
                v != null ? v.getCapacity() : 0,
                v != null ? v.getBabyFriendly() : false,
                v != null ? v.getPetFriendly() : false,
                driver.getIsActive(),
                driver.getIsBlocked(),
                driver.getBlockReason()
        );
    }

    @Transactional
    public DriverResponseDTO updateDriverProfile(String email, DriverResponseDTO dto) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (dto.getProfilePictureUrl() != null && !dto.getProfilePictureUrl().startsWith("http")) {
            String publicId = "driver_" + email.replace("@", "_").replace(".", "_");
            String imageUrl = cloudinaryService.uploadBase64Image(dto.getProfilePictureUrl(), publicId);
            driver.setProfilePictureUrl(imageUrl);
            dto.setProfilePictureUrl(imageUrl);
        }

        driver.setFirstName(dto.getFirstName());
        driver.setLastName(dto.getLastName());
        driver.setAddress(dto.getAddress());
        driver.setPhoneNumber(dto.getPhoneNumber());
        driver.setIsActive(dto.getIsActive());

        Vehicle v = driver.getVehicle();
        if (v != null) {
            v.setModel(dto.getVehicleModel());
            v.setLicensePlate(dto.getLicensePlate());
            v.setCapacity(dto.getPassengerCapacity());
            v.setBabyFriendly(dto.getBabyFriendly());
            v.setPetFriendly(dto.getPetFriendly());

            if (dto.getVehicleType() != null) {
                v.setType(rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType.valueOf(dto.getVehicleType().toUpperCase()));
            }
        }

        driverRepository.save(driver);
        return dto;
    }

    /**
     * Vozač se prijavi na sistem
     * - Postaje dostupan za dodelovanje vožnji SAMO ako je isActive = true
     * - LoggedIn se postavlja na true
     * - Nije automatski aktivan - može biti neaktivan nakon logovanja
     */
    @Transactional
    public void loginDriver(String email) {
        logger.info("=== DRIVER LOGIN START === Email: {}", email);
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (driver.getLoggedIn()) {
            logger.warn("Driver already logged in: {}", email);
            throw new RuntimeException("Driver is already logged in");
        }

        driver.setLoggedIn(true);
        logger.info("Setting loggedIn to true for: {}", email);

        // Kreiraj status event
        DriverStatusEvent event = new DriverStatusEvent(driver, DriverStatusEventType.LOGIN);
        driverStatusEventRepository.save(event);
        logger.info("Saved LOGIN event for: {}", email);

        driverRepository.save(driver);
        logger.info("=== DRIVER LOGIN SUCCESS === Email: {}", email);
    }

    /**
     * Vozač se odjavi iz sistema
     * - Ne može se odjaviti ako ima aktivnu vožnju
     * - Postaje nedostupan za dodelovanje vožnji
     * - Izračunava se koliko je bio aktivan i dodaje se u activeHours
     */
    @Transactional
    public void logoutDriver(String email) {
        logger.info("=== DRIVER LOGOUT START === Email: {}", email);
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Proveri da li ima aktivne vožnje
        List<Ride> activeRides = rideRepository.findByDriverAndRideStatus(
                driver,
                RideStatus.IN_PROGRESS
        );

        if (!activeRides.isEmpty()) {
            logger.warn("Cannot logout - driver has {} active ride(s): {}", activeRides.size(), email);
            throw new RuntimeException("Cannot logout while having an active ride");
        }

        // Izračunaj aktivne sate od poslednjeg logovanja
        LocalDateTime now = LocalDateTime.now();
        DriverStatusEvent lastLoginEvent = driverStatusEventRepository
                .findLastEventByDriverId(driver.getId());

        logger.info("Last login event: {}", lastLoginEvent != null ? lastLoginEvent.getEventType() : "null");

        if (lastLoginEvent != null &&
            (lastLoginEvent.getEventType() == DriverStatusEventType.LOGIN ||
             lastLoginEvent.getEventType() == DriverStatusEventType.ACTIVE)) {

            // Izračunaj trajanje od poslednjeg LOGIN/ACTIVE do sada
            Duration activeDuration = Duration.between(lastLoginEvent.getTimestamp(), now);
            long activeSeconds = activeDuration.getSeconds();

            logger.info("Active duration: {} seconds", activeSeconds);
            logger.info("Previous activeHours: {}", driver.getActiveHours());

            // Dodaj u activeHours (koja se čuva u sekundama)
            driver.setActiveHours(driver.getActiveHours() + activeSeconds);

            logger.info("Updated activeHours: {}", driver.getActiveHours());
        }

        driver.setLoggedIn(false);
        driver.setIsActive(false);

        // Kreiraj status event
        DriverStatusEvent event = new DriverStatusEvent(driver, DriverStatusEventType.LOGOUT);
        driverStatusEventRepository.save(event);
        logger.info("Saved LOGOUT event for: {}", email);

        driverRepository.save(driver);
        logger.info("=== DRIVER LOGOUT SUCCESS === Email: {}", email);
    }

    /**
     * Vozač manuelno menja svoj status (aktivan/neaktivan)
     * - Ako je vozač u toku vožnje, status se primenjuje nakon završetka vožnje
     * - Ako nema aktivne vožnje, status se menja odmah
     */
    @Transactional
    public void changeDriverStatus(String email, Boolean isActive) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Proveri da li ima aktivne vožnje
        List<Ride> activeRides = rideRepository.findByDriverAndRideStatus(
                driver,
                RideStatus.IN_PROGRESS
        );

        if (!activeRides.isEmpty()) {
            // Ako ima aktivnu vožnju, status će se promeniti nakon što se vožnja završi
            // Za sada, kreiramo event koji će beleži nameru promene
            DriverStatusEvent event = new DriverStatusEvent(
                    driver,
                    isActive ? DriverStatusEventType.ACTIVE : DriverStatusEventType.INACTIVE
            );
            driverStatusEventRepository.save(event);
            // Stvar će se faktički promeniti u ride service-u nakon završetka vožnje
        } else {
            // Nema aktivne vožnje, menja se odmah
            driver.setIsActive(isActive);

            DriverStatusEvent event = new DriverStatusEvent(
                    driver,
                    isActive ? DriverStatusEventType.ACTIVE : DriverStatusEventType.INACTIVE
            );
            driverStatusEventRepository.save(event);

            driverRepository.save(driver);
        }
    }

    /**
     * Izračunaj koliko sati je vozač bio aktivan u poslednja 24h
     * Gleda sve LOGIN/LOGOUT ili ACTIVE/INACTIVE događaje u poslednja 24h
     * @return aktivne sate kao broj (npr. 0.083 za 5 minuta)
     */
    @Transactional(readOnly = true)
    public Double getActiveHoursLast24h(String email) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last24h = now.minusHours(24);

        logger.info("Calculating active hours for {} from {} to {}", email, last24h, now);

        // Pronađi sve status događaje u poslednja 24h
        List<DriverStatusEvent> events = driverStatusEventRepository
                .findByDriverIdAndTimestampBetween(driver.getId(), last24h, now);

        logger.info("Found {} status events in last 24h", events.size());

        if (events.isEmpty()) {
            logger.info("No status events found, returning 0.0");
            return 0.0;
        }

        // Sortiraj po vremenu
        events.sort((e1, e2) -> e1.getTimestamp().compareTo(e2.getTimestamp()));

        long totalActiveSeconds = 0;
        LocalDateTime activeStartTime = null;

        for (DriverStatusEvent event : events) {
            logger.info("Processing event: {} at {}", event.getEventType(), event.getTimestamp());

            if (event.getEventType() == DriverStatusEventType.LOGIN ||
                event.getEventType() == DriverStatusEventType.ACTIVE) {
                // Počela je aktivnost
                if (activeStartTime == null) {
                    activeStartTime = event.getTimestamp();
                    logger.info("Activity started at {}", activeStartTime);
                }
            } else if (event.getEventType() == DriverStatusEventType.LOGOUT ||
                       event.getEventType() == DriverStatusEventType.INACTIVE) {
                // Završila se aktivnost
                if (activeStartTime != null) {
                    Duration duration = Duration.between(activeStartTime, event.getTimestamp());
                    long durationSeconds = duration.getSeconds();
                    totalActiveSeconds += durationSeconds;
                    logger.info("Activity ended. Duration: {} seconds ({} minutes)", durationSeconds, durationSeconds / 60);
                    activeStartTime = null;
                }
            }
        }

        // Ako je vozač trenutno aktivan (loggedIn=true), dodaj i vreme od poslednjeg LOGIN do sada
        if (activeStartTime != null && driver.getLoggedIn()) {
            Duration duration = Duration.between(activeStartTime, now);
            long durationSeconds = duration.getSeconds();
            totalActiveSeconds += durationSeconds;
            logger.info("Driver still logged in. Adding current active time: {} seconds ({} minutes)",
                       durationSeconds, durationSeconds / 60);
        }

        // Konvertuj sekunde u sate (double)
        double activeHours = totalActiveSeconds / 3600.0;
        logger.info("Total active hours in last 24h: {} ({} minutes)", activeHours, totalActiveSeconds / 60);

        return activeHours;
    }

    /**
     * Scheduled task: Obrisi stare driver status events (starije od 30 dana)
     * Izvršava se svaki dan u 02:00 ujutro
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldStatusEvents() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<DriverStatusEvent> oldEvents = driverStatusEventRepository
                .findByDriverIdAndTimestampBetween(null, LocalDateTime.MIN, thirtyDaysAgo);

        if (!oldEvents.isEmpty()) {
            // Trebam da dodam query za brisanje po vremenu
            // Za sada će biti obrisano kroz cascading
        }
    }
}