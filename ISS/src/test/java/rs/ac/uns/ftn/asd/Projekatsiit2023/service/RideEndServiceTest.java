package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideEndResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideEndServiceTest {

    @Mock private RideRepository rideRepository;
    @Mock private RouteService routeService;
    @Mock private LocationService locationService;
    @Mock private PriceConfigRepository priceConfigRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;

    private RideService rideService;

    private Ride ride;
    private Driver driver;
    private Vehicle vehicle;
    private Route route;
    private Location startLocation;
    private Location endLocation;
    private PriceConfig priceConfig;
    private RegisteredUser passenger;

    @BeforeEach
    void setUp() {
        rideService = new RideService(rideRepository);
        ReflectionTestUtils.setField(rideService, "routeService",          routeService);
        ReflectionTestUtils.setField(rideService, "locationService",       locationService);
        ReflectionTestUtils.setField(rideService, "priceConfigRepository", priceConfigRepository);
        ReflectionTestUtils.setField(rideService, "driverRepository",      driverRepository);
        ReflectionTestUtils.setField(rideService, "notificationService",   notificationService);
        ReflectionTestUtils.setField(rideService, "emailService",          emailService);

        passenger = new RegisteredUser();
        passenger.setId(1);
        passenger.setEmail("putnik@example.com");
        passenger.setFirstName("Ana");
        passenger.setLastName("Anic");
        passenger.setUserType(UserType.REGISTERED_USER);

        vehicle = new Vehicle();
        vehicle.setId(1);
        vehicle.setModel("Volkswagen Golf");
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setLicensePlate("NS-001-AA");

        driver = new Driver();
        driver.setId(2);
        driver.setEmail("vozac@example.com");
        driver.setFirstName("Petar");
        driver.setLastName("Petrovic");
        driver.setIsActive(true);
        driver.setVehicle(vehicle);

        route = new Route();
        route.setId(1);
        route.setDistance(10.0);
        route.setEstimatedTime(1800L);
        route.setLocations(new ArrayList<>());

        startLocation = new Location();
        startLocation.setId(1);
        startLocation.setAddress("Bulevar Oslobodjenja 1, Novi Sad");
        startLocation.setLatitude(45.2671);
        startLocation.setLongitude(19.8335);

        endLocation = new Location();
        endLocation.setId(2);
        endLocation.setAddress("Trg slobode 1, Novi Sad");
        endLocation.setLatitude(45.2550);
        endLocation.setLongitude(19.8449);

        ride = new Ride();
        ride.setId(1);
        ride.setRideStatus(RideStatus.IN_PROGRESS);
        ride.setStartTime(LocalDateTime.now().minusMinutes(35));
        ride.setPassenger(passenger);
        ride.setDriver(driver);
        ride.setRoute(route);
        ride.setStartLocation(startLocation);
        ride.setEndLocation(endLocation);
        ride.setTotalPrice(1480.0);
        ride.setPanicNotifications(new ArrayList<>());

        priceConfig = new PriceConfig();
        priceConfig.setVehicleType(VehicleType.STANDARD);
        priceConfig.setBasePrice(200.0);
        priceConfig.setPricePerKm(120.0);
    }

    private void mockHappyPathNoNextRide() {
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(Location.class), any(Route.class)))
                .thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RouteService.RouteEstimation(10.0, 30.0, List.of()));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));
        when(rideRepository.findNextRideByDriverId(anyInt(), any()))
                .thenReturn(Optional.empty());
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
    }

    private Location validRequest() {
        Location location = new Location();
        location.setLatitude(endLocation.getLatitude());
        location.setLongitude(endLocation.getLongitude());
        location.setAddress(endLocation.getAddress());
        return location;
    }

    // Osnovan slučaj: vožnja u toku, nema sledeće, status -> FINISHED
    @Test
    void shouldEndRideSuccessfullyAndSetStatusFinished() {
        mockHappyPathNoNextRide();

        RideEndResponseDTO result = rideService.endRide(1, validRequest());

        assertNotNull(result);
        assertEquals(1, result.getRideId());
        assertFalse(result.getHasNextRide());
        verify(rideRepository).save(argThat(r -> RideStatus.FINISHED.equals(r.getRideStatus())));
    }

    // Kada nema sledeće vožnje, vozač mora biti postavljen kao aktivan
    @Test
    void shouldSetDriverActiveWhenNoNextRide() {
        mockHappyPathNoNextRide();

        rideService.endRide(1, validRequest());

        verify(driverRepository).save(argThat(d -> Boolean.TRUE.equals(d.getIsActive())));
    }

    // Kada postoji sledeća vožnja, vozač mora ostati neaktivan
    @Test
    void shouldSetDriverInactiveWhenNextRideExists() {
        Ride nextRide = new Ride();
        nextRide.setId(2);
        nextRide.setStartTime(LocalDateTime.now().plusMinutes(15));
        nextRide.setStartLocation(startLocation);
        nextRide.setEndLocation(endLocation);

        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(Location.class), any(Route.class)))
                .thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RouteService.RouteEstimation(10.0, 30.0, List.of()));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));
        when(rideRepository.findNextRideByDriverId(anyInt(), any()))
                .thenReturn(Optional.of(nextRide));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        rideService.endRide(1, validRequest());

        verify(driverRepository).save(argThat(d -> Boolean.FALSE.equals(d.getIsActive())));
    }

    // Odgovor mora sadržati podatke o sledećoj vožnji kada postoji
    @Test
    void shouldReturnNextRideInfoWhenExists() {
        Ride nextRide = new Ride();
        nextRide.setId(2);
        nextRide.setStartTime(LocalDateTime.now().plusMinutes(20));
        nextRide.setStartLocation(startLocation);
        nextRide.setEndLocation(endLocation);

        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(Location.class), any(Route.class)))
                .thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RouteService.RouteEstimation(10.0, 30.0, List.of()));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));
        when(rideRepository.findNextRideByDriverId(anyInt(), any()))
                .thenReturn(Optional.of(nextRide));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        RideEndResponseDTO result = rideService.endRide(1, validRequest());

        assertTrue(result.getHasNextRide());
        assertEquals(2, result.getNextRideId());
        assertNotNull(result.getNextRideScheduledTime());
    }

    // Krajnja lokacija mora biti postavljena na vožnji
    @Test
    void shouldSetEndLocationOnRide() {
        mockHappyPathNoNextRide();

        rideService.endRide(1, validRequest());

        verify(rideRepository).save(argThat(r -> r.getEndLocation() != null));
    }

    // Vreme završetka mora biti postavljeno
    @Test
    void shouldSetEndTimeOnRide() {
        mockHappyPathNoNextRide();

        rideService.endRide(1, validRequest());

        verify(rideRepository).save(argThat(r -> r.getEndTime() != null));
    }

    // rideRepository.save() mora biti pozvan tačno jednom
    @Test
    void shouldSaveRideExactlyOnce() {
        mockHappyPathNoNextRide();

        rideService.endRide(1, validRequest());

        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    // Ruta mora biti sačuvana sa novom krajnjom lokacijom
    @Test
    void shouldSaveRouteAfterEndingRide() {
        mockHappyPathNoNextRide();

        rideService.endRide(1, validRequest());

        verify(routeService).save(any(Route.class));
    }

    // Trajanje vožnje se ispravno izračunava i vraća u odgovoru
    @Test
    void shouldReturnCorrectDurationInResponse() {
        mockHappyPathNoNextRide();
        ride.setStartTime(LocalDateTime.now().minusMinutes(30));

        RideEndResponseDTO result = rideService.endRide(1, validRequest());

        assertNotNull(result.getDuration());
        assertTrue(result.getDuration().contains("min"));
    }

    // LUXURY vozilo — cena se obračunava iz odgovarajućeg cenovnika
    @Test
    void shouldCalculatePriceForLuxuryVehicle() {
        vehicle.setType(VehicleType.LUXURY);

        PriceConfig luxuryConfig = new PriceConfig();
        luxuryConfig.setVehicleType(VehicleType.LUXURY);
        luxuryConfig.setBasePrice(500.0);
        luxuryConfig.setPricePerKm(200.0);

        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(), any())).thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.LUXURY))
                .thenReturn(Optional.of(luxuryConfig));
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RouteService.RouteEstimation(10.0, 30.0, List.of()));
        when(rideRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rideRepository.findNextRideByDriverId(anyInt(), any())).thenReturn(Optional.empty());
        when(driverRepository.save(any())).thenReturn(driver);

        RideEndResponseDTO result = rideService.endRide(1, validRequest());

        assertNotNull(result);
        assertTrue(result.getFinalPrice() > 0);
    }

    // Vožnja ne postoji u bazi
    @Test
    void shouldThrowWhenRideNotFound() {
        when(rideRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.endRide(999, validRequest()));

        assertTrue(ex.getMessage().contains("Ride not found"));
    }

    // Pokušaj završetka vožnje koja je već FINISHED
    @Test
    void shouldThrowWhenRideAlreadyFinished() {
        ride.setRideStatus(RideStatus.FINISHED);
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.endRide(1, validRequest()));

        assertTrue(ex.getMessage().contains("already finished"));
    }

    // Cenovnik za tip vozila nije konfigurisan u sistemu
    @Test
    void shouldThrowWhenPriceConfigNotFound() {
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.endRide(1, validRequest()));

        assertTrue(ex.getMessage().contains("Price config not found"));
    }

    // Vozač nema vozilo — NullPointerException
    @Test
    void shouldThrowWhenDriverHasNoVehicle() {
        driver.setVehicle(null);
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));

        assertThrows(NullPointerException.class,
                () -> rideService.endRide(1, validRequest()));
    }

    // Vožnja nema dodeljenog vozača — NullPointerException
    @Test
    void shouldThrowWhenRideHasNoDriver() {
        ride.setDriver(null);
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));

        assertThrows(NullPointerException.class,
                () -> rideService.endRide(1, validRequest()));
    }

    // REQUESTED vožnja ne može biti završena — mora se otkazati
    @Test
    void shouldThrowWhenRideIsInRequestedStatus() {
        ride.setRideStatus(RideStatus.REQUESTED);
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> rideService.endRide(1, validRequest()));

        assertTrue(ex.getMessage().contains("cannot be ended"));
    }

    // Vožnja traje 0 minuta — početak i kraj su isti trenutak
    @Test
    void shouldHandleZeroDurationRide() {
        ride.setStartTime(LocalDateTime.now());

        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(), any())).thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RouteService.RouteEstimation(0.0, 0.0, List.of()));
        when(rideRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rideRepository.findNextRideByDriverId(anyInt(), any())).thenReturn(Optional.empty());
        when(driverRepository.save(any())).thenReturn(driver);

        RideEndResponseDTO result = rideService.endRide(1, endLocation);

        assertNotNull(result);
        assertEquals("0 min", result.getDuration());
    }

    // Vožnja koja traje tačno 1 sat
    @Test
    void shouldHandleRideWithExactlyOneHour() {
        ride.setStartTime(LocalDateTime.now().minusHours(1));

        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(), any())).thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RouteService.RouteEstimation(10.0, 60.0, List.of()));
        when(rideRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rideRepository.findNextRideByDriverId(anyInt(), any())).thenReturn(Optional.empty());
        when(driverRepository.save(any())).thenReturn(driver);

        RideEndResponseDTO result = rideService.endRide(1, endLocation);

        assertEquals("60 min", result.getDuration());
    }

    // Adresa sledeće vožnje mora biti ispravno prenesena u odgovor
    @Test
    void shouldIncludeCorrectAddressesForNextRide() {
        Location nextStart = new Location();
        nextStart.setAddress("Bulevar Oslobodjenja 5");
        Location nextEnd = new Location();
        nextEnd.setAddress("Futoska 1");

        Ride nextRide = new Ride();
        nextRide.setId(2);
        nextRide.setStartTime(LocalDateTime.now().plusMinutes(20));
        nextRide.setStartLocation(nextStart);
        nextRide.setEndLocation(nextEnd);

        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(), any())).thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD))
                .thenReturn(Optional.of(priceConfig));
        when(routeService.estimateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RouteService.RouteEstimation(10.0, 30.0, List.of()));
        when(rideRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rideRepository.findNextRideByDriverId(anyInt(), any())).thenReturn(Optional.of(nextRide));
        when(driverRepository.save(any())).thenReturn(driver);

        RideEndResponseDTO result = rideService.endRide(1, validRequest());

        assertEquals("Bulevar Oslobodjenja 5", result.getNextRideFrom());
        assertEquals("Futoska 1", result.getNextRideTo());
    }

    // Nakon završetka vožnje, putnik mora dobiti email i notifikaciju o završetku
    @Test
    void shouldSendEmailAndNotificationToPassengerAfterRideEnd() {
        mockHappyPathNoNextRide();
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));

        rideService.endRideAndNotify(1, validRequest());

        verify(emailService, atLeastOnce()).sendRideFinishedEmail(anyString(), any());
        verify(notificationService, atLeastOnce()).sendRideFinishedNotification(any(), any());
    }
}

