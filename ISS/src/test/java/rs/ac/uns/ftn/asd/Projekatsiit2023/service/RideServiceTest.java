package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.RideRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideCreatedResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UNIT TESTOVI za RideService.createNewRide (2.4.1 Porucivanje voznje)
 * Testiramo:
 *   - Sva biznis logika unutar createNewRide metode
 *   - Pronalazak i dodela vozaca
 *   - Validacija putnika, ulinkovanih, lokacija, tipa vozila
 *   - Racunanje cene

 *   - sve zavimosti su mokovane repo + servisi i testiramo samo logiku unutar RideService
 */

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock private RideRepository rideRepository;
    @Mock private RouteService routeService;
    @Mock private RegisteredUserRepository userRepository;
    @Mock private LocationService locationService;
    @Mock private PriceConfigRepository priceConfigRepository;
    @Mock private PanicNotificationRepository panicNotificationRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;

    private RideService rideService;

    private RegisteredUser passenger;
    private Driver driver;
    private Vehicle vehicle;
    private PriceConfig priceConfig;
    private RouteService.RouteEstimation routeEstimation;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    void setUp() {
        // RideService se kreira rucno, konstruktor prima samo RideRepository
        rideService = new RideService(rideRepository);
        ReflectionTestUtils.setField(rideService, "routeService",               routeService);
        ReflectionTestUtils.setField(rideService, "userRepository",             userRepository);
        ReflectionTestUtils.setField(rideService, "locationService",            locationService);
        ReflectionTestUtils.setField(rideService, "priceConfigRepository",      priceConfigRepository);
        ReflectionTestUtils.setField(rideService, "panicNotificationRepository",panicNotificationRepository);
        ReflectionTestUtils.setField(rideService, "driverRepository",           driverRepository);
        ReflectionTestUtils.setField(rideService, "notificationService", notificationService);
        ReflectionTestUtils.setField(rideService, "emailService", emailService);

        // --- Korisnik ---
        passenger = new RegisteredUser();
        passenger.setId(1);
        passenger.setEmail("korisnik@example.com");
        passenger.setFirstName("Ana");
        passenger.setLastName("Anic");
        passenger.setPassword("password");
        passenger.setIsBlocked(false);
        passenger.setUserType(UserType.REGISTERED_USER);

        // --- Vozilo ---
        vehicle = new Vehicle();
        vehicle.setId(1);
        vehicle.setModel("Volkswagen Golf");
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setLicensePlate("NS-001-AA");
        vehicle.setCapacity(4);
        vehicle.setBabyFriendly(true);
        vehicle.setPetFriendly(true);
        vehicle.setCurrentLatitude(45.2671); // blizu polazista
        vehicle.setCurrentLongitude(19.8335);

        // --- Vozac ---
        driver = new Driver();
        driver.setId(2);
        driver.setEmail("vozac@example.com");
        driver.setFirstName("Petar");
        driver.setLastName("Petrovic");
        driver.setPassword("password");
        driver.setIsBlocked(false);
        driver.setIsActive(true);
        driver.setActiveHours(0L); // nista nije radio
        driver.setVehicle(vehicle);
        driver.setUserType(UserType.DRIVER);
        driver.setRatings(new ArrayList<>());
        driver.setAssignedRides(new ArrayList<>());

        // --- Cenovnik za STANDARD ---
        priceConfig = new PriceConfig();
        priceConfig.setVehicleType(VehicleType.STANDARD);
        priceConfig.setBasePrice(200.0);
        priceConfig.setPricePerKm(120.0);
        // cena = 200 + 10km * 120 = 1400 RSD

        // --- Mock odgovor ORS APIja ---
        routeEstimation = new RouteService.RouteEstimation(10.0, 30.0, List.of());
        // 10km, 30 minuta
    }

    // ----------------------------------------------------------------
    // POMOCNE METODE
    // ----------------------------------------------------------------

    //kreiramo validan zahtev 2 lokacije, STANDARD, 1h unapred
    private RideRequestDTO validRequest() {
        return new RideRequestDTO(
                List.of(
                        new RideRequestDTO.LocationDTO("Bulevar Oslobodjenja 1", 45.2671, 19.8335),
                        new RideRequestDTO.LocationDTO("Trg slobode 1",          45.2550, 19.8449)
                ),
                Collections.emptyList(),
                "STANDARD",
                false,
                false,
                LocalDateTime.now().plusHours(1).format(FMT)
        );
    }

    //postavljamo sve mokove, vozac je dostupan
    private void mockHappyPath() {
        when(userRepository.findByEmail("korisnik@example.com"))
                .thenReturn(Optional.of(passenger));

        Route savedRoute = new Route();
        savedRoute.setId(1);
        savedRoute.setDistance(10.0);
        savedRoute.setEstimatedTime(1800L);
        savedRoute.setLocations(new ArrayList<>());

        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(routeEstimation);
        when(routeService.save(any(Route.class))).thenReturn(savedRoute);
        when(locationService.saveLocation(any(Location.class))).thenAnswer(inv -> {
            Location l = inv.getArgument(0);
            l.setId(99);
            return l;
        });
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(driverRepository.findAll()).thenReturn(List.of(driver));
        when(rideRepository.findByDriverIdAndRideStatusIn(anyInt(), any()))
                .thenReturn(Collections.emptyList());
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(10);
            return r;
        });
    }

    // ================================================================
    // POZITIVNI TESTOVI
    // ================================================================

    //osnovni slucaj: validan zahtev, putnik postoji, vozac dostupan
    //provera da se voznja kreira i vraca status REQUESTED
    @Test
    void shouldCreateRideSuccessfullyWithMinimalData() {
        mockHappyPath();

        RideCreatedResponseDTO result =
                rideService.createNewRide(validRequest(), "korisnik@example.com");

        assertNotNull(result);
        assertEquals("REQUESTED", result.getStatus());
        // Verifikujemo da je rideRepository.save() pozvan tacno jednom
        verify(rideRepository).save(any(Ride.class));
    }

    //provera da je vozac stvarno dodeljen
    @Test
    void shouldAssignDriverWhenCreatingRide() {
        mockHappyPath();

        RideCreatedResponseDTO result =
                rideService.createNewRide(validRequest(), "korisnik@example.com");

        assertNotNull(result.getDriverName());
        assertFalse(result.getDriverName().isBlank());
    }

    //provera cene
    @Test
    void shouldCalculatePriceCorrectly() {
        mockHappyPath();

        RideCreatedResponseDTO result =
                rideService.createNewRide(validRequest(), "korisnik@example.com");

        assertTrue(result.getEstimatedPrice() > 0);
    }

    //distanca i trajanje iz procene se ispravno prenose u odgovor
    @Test
    void shouldReturnDistanceAndDuration() {
        mockHappyPath();

        RideCreatedResponseDTO result =
                rideService.createNewRide(validRequest(), "korisnik@example.com");

        assertEquals(10.0, result.getDistanceKm());
        assertEquals(30.0, result.getDurationMin());
    }

    //vznja sa registrovanim ulinkovanim putnikom
    @Test
    void shouldCreateRideWithValidCoPassenger() {
        mockHappyPath();

        RegisteredUser coPassenger = new RegisteredUser();
        coPassenger.setId(3);
        coPassenger.setEmail("putnik@example.com");
        coPassenger.setPassword("password");
        coPassenger.setIsBlocked(false);
        coPassenger.setUserType(UserType.REGISTERED_USER);
        when(userRepository.findByEmail("putnik@example.com"))
                .thenReturn(Optional.of(coPassenger));

        RideRequestDTO req = validRequest();
        req.setPassengerEmails(List.of("putnik@example.com"));

        RideCreatedResponseDTO result =
                rideService.createNewRide(req, "korisnik@example.com");

        assertNotNull(result);
        assertEquals("REQUESTED", result.getStatus());
    }

    //voznja sa tri stanice, redosled bitan
    @Test
    void shouldCreateRideWithThreeIntermediateStops() {
        mockHappyPath();

        RideRequestDTO req = validRequest();
        req.setLocations(List.of(
                new RideRequestDTO.LocationDTO("Start", 45.26, 19.83),
                new RideRequestDTO.LocationDTO("Stop1", 45.27, 19.84),
                new RideRequestDTO.LocationDTO("Stop2", 45.28, 19.85),
                new RideRequestDTO.LocationDTO("Stop3", 45.29, 19.86),
                new RideRequestDTO.LocationDTO("End",   45.30, 19.87)
        ));

        assertNotNull(rideService.createNewRide(req, "korisnik@example.com"));
    }

    //baby friendly filter, vozac mora imati baby friendly vozilo
    @Test
    void shouldCreateRideWithBabyFriendlyOption() {
        mockHappyPath();
        RideRequestDTO req = validRequest();
        req.setBabyFriendly(true);

        RideCreatedResponseDTO result =
                rideService.createNewRide(req, "korisnik@example.com");

        assertNotNull(result);
        assertEquals("REQUESTED", result.getStatus());
    }

    //pet friendly filter, vozac mora imati pet friendly vozilo
    @Test
    void shouldCreateRideWithPetFriendlyOption() {
        mockHappyPath();
        RideRequestDTO req = validRequest();
        req.setPetFriendly(true);

        RideCreatedResponseDTO result =
                rideService.createNewRide(req, "korisnik@example.com");

        assertNotNull(result);
        assertEquals("REQUESTED", result.getStatus());
    }

    //zakazana voznja 4h 59min unapred, max je 5h unapred
    @Test
    void shouldCreateScheduledRideUpToFiveHoursAhead() {
        mockHappyPath();
        RideRequestDTO req = validRequest();
        req.setScheduledTime(LocalDateTime.now().plusHours(4).plusMinutes(59).format(FMT));

        assertNotNull(rideService.createNewRide(req, "korisnik@example.com"));
    }

    //LUXURY vozilo sa odgovarajucim cenovnilom
    @Test
    void shouldCreateRideWithLuxuryVehicleType() {
        vehicle.setType(VehicleType.LUXURY);

        PriceConfig luxuryPrice = new PriceConfig();
        luxuryPrice.setVehicleType(VehicleType.LUXURY);
        luxuryPrice.setBasePrice(500.0);
        luxuryPrice.setPricePerKm(200.0);

        when(userRepository.findByEmail("korisnik@example.com"))
                .thenReturn(Optional.of(passenger));
        Route savedRoute = new Route();
        savedRoute.setId(1);
        savedRoute.setDistance(10.0);
        savedRoute.setEstimatedTime(1800L);
        savedRoute.setLocations(new ArrayList<>());
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(routeEstimation);
        when(routeService.save(any(Route.class))).thenReturn(savedRoute);
        when(locationService.saveLocation(any(Location.class))).thenAnswer(inv -> {
            Location l = inv.getArgument(0);
            l.setId(99);
            return l;
        });
        when(priceConfigRepository.findByVehicleType(VehicleType.LUXURY))
                .thenReturn(Optional.of(luxuryPrice));
        when(driverRepository.findAll()).thenReturn(List.of(driver));
        when(rideRepository.findByDriverIdAndRideStatusIn(anyInt(), any()))
                .thenReturn(Collections.emptyList());
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(20);
            return r;
        });

        RideRequestDTO req = validRequest();
        req.setVehicleType("LUXURY");

        RideCreatedResponseDTO result =
                rideService.createNewRide(req, "korisnik@example.com");

        assertNotNull(result);
        assertTrue(result.getEstimatedPrice() > 0);
    }

    //da se rideRepository.save() stvarno poziva
    @Test
    void shouldSaveRideToRepository() {
        mockHappyPath();
        rideService.createNewRide(validRequest(), "korisnik@example.com");
        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    // ================================================================
    // NEGATIVNI TESTOVI
    // ================================================================

    //putnik ne postoji u bazi
    @Test
    void shouldThrowWhenPassengerNotFound() {
        when(userRepository.findByEmail("nepostoji@example.com"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(validRequest(), "nepostoji@example.com"));

        assertTrue(ex.getMessage().contains("Passenger not found"));
    }

    //blokiran putnik ne sme poruciti voznju
    //provera poruke koja sadrzi razlog blokiranja
    @Test
    void shouldThrowWhenPassengerIsBlocked() {
        passenger.setIsBlocked(true);
        passenger.setBlockReason("Krsenje pravila");
        when(userRepository.findByEmail("korisnik@example.com"))
                .thenReturn(Optional.of(passenger));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(validRequest(), "korisnik@example.com"));

        assertTrue(ex.getMessage().contains("blocked"));
    }

    //nije validna samo jedna lokacija
    @Test
    void shouldThrowWhenLessThanTwoLocations() {
        RideRequestDTO req = validRequest();
        req.setLocations(List.of(
                new RideRequestDTO.LocationDTO("Samo start", 45.26, 19.83)));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(req, "korisnik@example.com"));

        assertTrue(ex.getMessage().contains("At least start and end location are required"));
    }

    //null lista lokacija
    @Test
    void shouldThrowWhenLocationsListIsNull() {
        RideRequestDTO req = validRequest();
        req.setLocations(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(req, "korisnik@example.com"));

        assertTrue(ex.getMessage().contains("At least start and end location are required"));
    }

    //u sistemu nema vozaca
    @Test
    void shouldThrowWhenNoAvailableDrivers() {
        when(userRepository.findByEmail("korisnik@example.com"))
                .thenReturn(Optional.of(passenger));
        Route savedRoute = new Route();
        savedRoute.setId(1);
        savedRoute.setDistance(10.0);
        savedRoute.setEstimatedTime(1800L);
        savedRoute.setLocations(new ArrayList<>());
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(routeEstimation);
        when(routeService.save(any(Route.class))).thenReturn(savedRoute);
        when(locationService.saveLocation(any(Location.class))).thenAnswer(inv -> {
            Location l = inv.getArgument(0);
            l.setId(99);
            return l;
        });
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(driverRepository.findAll()).thenReturn(Collections.emptyList());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(validRequest(), "korisnik@example.com"));

        assertTrue(ex.getMessage().contains("No available drivers"));
    }

    //svo vozaci su neaktivni
    @Test
    void shouldThrowWhenAllDriversAreInactive() {
        driver.setIsActive(false);

        when(userRepository.findByEmail("korisnik@example.com"))
                .thenReturn(Optional.of(passenger));
        Route savedRoute = new Route();
        savedRoute.setId(1);
        savedRoute.setDistance(10.0);
        savedRoute.setEstimatedTime(1800L);
        savedRoute.setLocations(new ArrayList<>());
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(routeEstimation);
        when(routeService.save(any(Route.class))).thenReturn(savedRoute);
        when(locationService.saveLocation(any(Location.class))).thenAnswer(inv -> {
            Location l = inv.getArgument(0);
            l.setId(99);
            return l;
        });
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(driverRepository.findAll()).thenReturn(List.of(driver));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(validRequest(), "korisnik@example.com"));

        assertTrue(ex.getMessage().contains("No available drivers"));
    }

    //svi vzaci su blokirani
    @Test
    void shouldThrowWhenAllDriversAreBlocked() {
        driver.setIsBlocked(true);

        when(userRepository.findByEmail("korisnik@example.com"))
                .thenReturn(Optional.of(passenger));
        Route savedRoute = new Route();
        savedRoute.setId(1);
        savedRoute.setDistance(10.0);
        savedRoute.setEstimatedTime(1800L);
        savedRoute.setLocations(new ArrayList<>());
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(routeEstimation);
        when(routeService.save(any(Route.class))).thenReturn(savedRoute);
        when(locationService.saveLocation(any(Location.class))).thenAnswer(inv -> {
            Location l = inv.getArgument(0);
            l.setId(99);
            return l;
        });
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(driverRepository.findAll()).thenReturn(List.of(driver));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(validRequest(), "korisnik@example.com"));

        assertTrue(ex.getMessage().contains("No available drivers"));
    }

    //vozac premasio 8h da je aktivan
    @Test
    void shouldThrowWhenDriverExceeds8WorkingHours() {
        driver.setActiveHours(28801L); // 8h 1s

        when(userRepository.findByEmail("korisnik@example.com"))
                .thenReturn(Optional.of(passenger));
        Route savedRoute = new Route();
        savedRoute.setId(1);
        savedRoute.setDistance(10.0);
        savedRoute.setEstimatedTime(1800L);
        savedRoute.setLocations(new ArrayList<>());
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(routeEstimation);
        when(routeService.save(any(Route.class))).thenReturn(savedRoute);
        when(locationService.saveLocation(any(Location.class))).thenAnswer(inv -> {
            Location l = inv.getArgument(0);
            l.setId(99);
            return l;
        });
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(driverRepository.findAll()).thenReturn(List.of(driver));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(validRequest(), "korisnik@example.com"));

        assertTrue(ex.getMessage().contains("No available drivers"));
    }

    //ulinkovani putnik nije registrovan
    @Test
    void shouldThrowWhenCoPassengerNotRegistered() {
        when(userRepository.findByEmail("neregistrovan@example.com"))
                .thenReturn(Optional.empty());

        RideRequestDTO req = validRequest();
        req.setPassengerEmails(List.of("neregistrovan@example.com"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(req, "korisnik@example.com"));

        assertTrue(ex.getMessage().contains("not registered users"));
    }

    //nevazeci tip vozila
    @Test
    void shouldThrowWhenInvalidVehicleType() {
        when(userRepository.findByEmail("korisnik@example.com"))
                .thenReturn(Optional.of(passenger));
        Route savedRoute = new Route();
        savedRoute.setId(1);
        savedRoute.setDistance(10.0);
        savedRoute.setEstimatedTime(1800L);
        savedRoute.setLocations(new ArrayList<>());
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(routeEstimation);
        when(routeService.save(any(Route.class))).thenReturn(savedRoute);
        when(locationService.saveLocation(any(Location.class))).thenAnswer(inv -> {
            Location l = inv.getArgument(0);
            l.setId(99);
            return l;
        });

        RideRequestDTO req = validRequest();
        req.setVehicleType("NEPOSTOJECI");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(req, "korisnik@example.com"));

        assertTrue(ex.getMessage().contains("Invalid vehicle type"));
    }

    //ako ORS API vraca null da ne moze proceniti rutu
    //servis ne sme nastaviti sa null rutom
    @Test
    void shouldThrowWhenRouteEstimationFails() {
        when(userRepository.findByEmail("korisnik@example.com"))
                .thenReturn(Optional.of(passenger));
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(validRequest(), "korisnik@example.com"));

        assertTrue(ex.getMessage().contains("Could not estimate route"));
    }

    //vozac nema vozilo
    @Test
    void shouldThrowWhenDriverHasNoVehicle() {
        driver.setVehicle(null);

        when(userRepository.findByEmail("korisnik@example.com"))
                .thenReturn(Optional.of(passenger));
        Route savedRoute = new Route();
        savedRoute.setId(1);
        savedRoute.setDistance(10.0);
        savedRoute.setEstimatedTime(1800L);
        savedRoute.setLocations(new ArrayList<>());
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(routeEstimation);
        when(routeService.save(any(Route.class))).thenReturn(savedRoute);
        when(locationService.saveLocation(any(Location.class))).thenAnswer(inv -> {
            Location l = inv.getArgument(0);
            l.setId(99);
            return l;
        });
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(driverRepository.findAll()).thenReturn(List.of(driver));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(validRequest(), "korisnik@example.com"));

        assertTrue(ex.getMessage().contains("No available drivers"));
    }

    // ================================================================
    // GRANICNI SLUCAJEVI
    // ================================================================

    //vozac ima tacno 8h
    @Test
    void shouldSucceedWhenDriverWorkingHoursExactly8Hours() {
        driver.setActiveHours(28800L); // tacno 8h — po specifikaciji DOZVOLJENO

        when(userRepository.findByEmail("korisnik@example.com"))
                .thenReturn(Optional.of(passenger));
        Route savedRoute = new Route();
        savedRoute.setId(1);
        savedRoute.setDistance(10.0);
        savedRoute.setEstimatedTime(1800L);
        savedRoute.setLocations(new ArrayList<>());
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(routeEstimation);
        when(routeService.save(any(Route.class))).thenReturn(savedRoute);
        when(locationService.saveLocation(any(Location.class))).thenAnswer(inv -> {
            Location l = inv.getArgument(0);
            l.setId(99);
            return l;
        });
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(driverRepository.findAll()).thenReturn(List.of(driver));
        when(rideRepository.findByDriverIdAndRideStatusIn(anyInt(), any()))
                .thenReturn(Collections.emptyList());
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(10);
            return r;
        });

        RideCreatedResponseDTO result =
                rideService.createNewRide(validRequest(), "korisnik@example.com");

        assertNotNull(result);
        assertEquals("REQUESTED", result.getStatus());
    }

    //ima 7h 59m 59s tj 28799 sekundi
    @Test
    void shouldSucceedWhenDriverWorkingHoursJustUnder8Hours() {
        driver.setActiveHours(28799L); // 1 sekunda ispod 8h

        when(userRepository.findByEmail("korisnik@example.com"))
                .thenReturn(Optional.of(passenger));
        Route savedRoute = new Route();
        savedRoute.setId(1);
        savedRoute.setDistance(10.0);
        savedRoute.setEstimatedTime(1800L);
        savedRoute.setLocations(new ArrayList<>());
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(routeEstimation);
        when(routeService.save(any(Route.class))).thenReturn(savedRoute);
        when(locationService.saveLocation(any(Location.class))).thenAnswer(inv -> {
            Location l = inv.getArgument(0);
            l.setId(99);
            return l;
        });
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(driverRepository.findAll()).thenReturn(List.of(driver));
        when(rideRepository.findByDriverIdAndRideStatusIn(anyInt(), any()))
                .thenReturn(Collections.emptyList());
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(10);
            return r;
        });

        RideCreatedResponseDTO result =
                rideService.createNewRide(validRequest(), "korisnik@example.com");

        assertNotNull(result);
        assertEquals("REQUESTED", result.getStatus());
    }

    //vozac je trenutno u toku voznje i ima vise od 10 minuta do kraja
    //Specifikacija: vozac koji ima manje od 10min do kraja MOZE biti dodeljen ali onaj sa vise od 10min NE MOZE
    @Test
    void shouldThrowWhenDriverCurrentlyInProgressAndMoreThan10MinLeft() {
        Ride activeRide = new Ride();
        activeRide.setId(5);
        activeRide.setRideStatus(RideStatus.IN_PROGRESS);
        activeRide.setStartTime(LocalDateTime.now().minusMinutes(5));
        Route activeRoute = new Route();
        activeRoute.setEstimatedTime(3600L); // 60min ukupno => jos 55min
        activeRide.setRoute(activeRoute);

        when(userRepository.findByEmail("korisnik@example.com"))
                .thenReturn(Optional.of(passenger));
        Route savedRoute = new Route();
        savedRoute.setId(1);
        savedRoute.setDistance(10.0);
        savedRoute.setEstimatedTime(1800L);
        savedRoute.setLocations(new ArrayList<>());
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(routeEstimation);
        when(routeService.save(any(Route.class))).thenReturn(savedRoute);
        when(locationService.saveLocation(any(Location.class))).thenAnswer(inv -> {
            Location l = inv.getArgument(0);
            l.setId(99);
            return l;
        });
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(driverRepository.findAll()).thenReturn(List.of(driver));
        when(rideRepository.findByDriverIdAndRideStatusIn(eq(driver.getId()), any()))
                .thenReturn(List.of(activeRide));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(validRequest(), "korisnik@example.com"));

        assertTrue(ex.getMessage().contains("No available drivers"));
    }

    //vise ulinkovanih i jedan nije registrovan, svi moraju biti reg
    @Test
    void shouldThrowWhenMultipleCoPassengersAndSomeNotRegistered() {
        RegisteredUser validCoPassenger = new RegisteredUser();
        validCoPassenger.setEmail("validan@example.com");
        validCoPassenger.setPassword("password");
        validCoPassenger.setIsBlocked(false);
        validCoPassenger.setUserType(UserType.REGISTERED_USER);
        when(userRepository.findByEmail("validan@example.com"))
                .thenReturn(Optional.of(validCoPassenger));
        when(userRepository.findByEmail("nepostoji@example.com"))
                .thenReturn(Optional.empty());

        RideRequestDTO req = validRequest();
        req.setPassengerEmails(List.of("validan@example.com", "nepostoji@example.com"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.createNewRide(req, "korisnik@example.com"));

        assertTrue(ex.getMessage().contains("not registered users"));
        assertTrue(ex.getMessage().contains("nepostoji@example.com"));
    }

    //sistem birda BLIZEG vozaca kada ima vise kandidata
    //vozac "driver" je na 45.26, "farDriver" je na 48.0 daleko, ocekujemo prvog
    @Test
    void shouldChooseNearestDriverWhenMultipleAvailable() {
        Vehicle farVehicle = new Vehicle();
        farVehicle.setId(2);
        farVehicle.setType(VehicleType.STANDARD);
        farVehicle.setModel("Skoda Octavia");
        farVehicle.setLicensePlate("NS-999-ZZ");
        farVehicle.setCapacity(4);
        farVehicle.setBabyFriendly(false);
        farVehicle.setPetFriendly(false);
        farVehicle.setCurrentLatitude(48.0); // daleko
        farVehicle.setCurrentLongitude(19.8);

        Driver farDriver = new Driver();
        farDriver.setId(10);
        farDriver.setEmail("daleki@example.com");
        farDriver.setFirstName("Daleki");
        farDriver.setLastName("Vozac");
        farDriver.setPassword("password");
        farDriver.setIsBlocked(false);
        farDriver.setIsActive(true);
        farDriver.setActiveHours(0L);
        farDriver.setVehicle(farVehicle);
        farDriver.setRatings(new ArrayList<>());
        farDriver.setAssignedRides(new ArrayList<>());

        mockHappyPath();
        when(driverRepository.findAll()).thenReturn(List.of(farDriver, driver));
        when(rideRepository.findByDriverIdAndRideStatusIn(eq(driver.getId()), any()))
                .thenReturn(Collections.emptyList());
        when(rideRepository.findByDriverIdAndRideStatusIn(eq(farDriver.getId()), any()))
                .thenReturn(Collections.emptyList());

        RideCreatedResponseDTO result =
                rideService.createNewRide(validRequest(), "korisnik@example.com");

        assertNotNull(result);
        // blizi vozac Petar Petrovic treba biti dodeljen
        assertEquals("Petar Petrovic", result.getDriverName());
    }
}