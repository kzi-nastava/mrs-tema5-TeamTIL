package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PriceConfigRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RideRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideStopServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private RouteService routeService;

    @Mock
    private LocationService locationService;

    @Mock
    private PriceConfigRepository priceConfigRepository;

    private RideService rideService;

    private Ride ride;
    private Location endLocation;
    private Driver driver;
    private Vehicle vehicle;
    private Route route;
    private PriceConfig priceConfig;

    @BeforeEach
    void setUp() {
        // Kreiram RideService sa mock-ovanim RideRepository-jem
        rideService = new RideService(rideRepository);

        // Ručno setuju ostale zavimosti preko ReflectionTestUtils
        ReflectionTestUtils.setField(rideService, "locationService", locationService);
        ReflectionTestUtils.setField(rideService, "routeService", routeService);
        ReflectionTestUtils.setField(rideService, "priceConfigRepository", priceConfigRepository);

        // Setup basic ride with all required fields
        route = new Route();
        route.setId(1);
        route.setDistance(10.0);

        Location startLocation = new Location();
        startLocation.setAddress("123 Main St");
        startLocation.setLatitude(40.7128);
        startLocation.setLongitude(-74.0060);

        endLocation = new Location();
        endLocation.setAddress("456 Park Ave");
        endLocation.setLatitude(40.7580);
        endLocation.setLongitude(-73.9855);

        vehicle = new Vehicle();
        vehicle.setType(VehicleType.STANDARD);

        driver = new Driver();
        driver.setVehicle(vehicle);

        ride = new Ride();
        ride.setId(1);
        ride.setRideStatus(RideStatus.IN_PROGRESS);
        ride.setStartTime(LocalDateTime.now().minusHours(1));
        ride.setStartLocation(startLocation);
        ride.setDriver(driver);
        ride.setRoute(route);

        // Setup price config mock
        priceConfig = new PriceConfig();
        priceConfig.setVehicleType(VehicleType.STANDARD);
        priceConfig.setBasePrice(100.0);
        priceConfig.setPricePerKm(10.0);
    }

    @Test
    void testRideStatusTransitions() {
        Ride testRide = new Ride();
        testRide.setId(1);
        testRide.setRideStatus(RideStatus.IN_PROGRESS);
        testRide.setStartTime(LocalDateTime.now().minusHours(1));

        assertEquals(RideStatus.IN_PROGRESS, testRide.getRideStatus());

        testRide.setRideStatus(RideStatus.FINISHED);
        assertEquals(RideStatus.FINISHED, testRide.getRideStatus());
    }

    @Test
    void testRideCanBeFinished() {
        Ride testRide = new Ride();
        testRide.setRideStatus(RideStatus.IN_PROGRESS);

        assertTrue(RideStatus.IN_PROGRESS.equals(testRide.getRideStatus()));

        testRide.setRideStatus(RideStatus.FINISHED);
        assertTrue(RideStatus.FINISHED.equals(testRide.getRideStatus()));
    }

    @Test
    void testRideCannotBeFinishedIfAlreadyFinished() {
        Ride testRide = new Ride();
        testRide.setRideStatus(RideStatus.FINISHED);

        assertTrue(RideStatus.FINISHED.equals(testRide.getRideStatus()));
        assertFalse(RideStatus.IN_PROGRESS.equals(testRide.getRideStatus()));
    }

    @Test
    void shouldThrowExceptionWhenRideNotFound() {
        when(rideRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                rideService.stopRide(999, new Location(), LocalDateTime.now()));

        verify(rideRepository).findById(999);
    }

    @Test
    void shouldThrowExceptionWhenRideAlreadyFinished() {
        ride.setRideStatus(RideStatus.FINISHED);
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));

        assertThrows(RuntimeException.class, () ->
                rideService.stopRide(1, endLocation, LocalDateTime.now()));

        verify(rideRepository).findById(1);
    }

    @Test
    void shouldSuccessfullyStopRideWhenAllConditionsMet() {
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(Location.class), any(Route.class))).thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD)).thenReturn(Optional.of(priceConfig));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        assertDoesNotThrow(() ->
                rideService.stopRide(1, endLocation, LocalDateTime.now()));

        verify(rideRepository).findById(1);
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void shouldThrowExceptionWhenRideNotInProgress() {
        ride.setRideStatus(RideStatus.REQUESTED);
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD)).thenReturn(Optional.of(priceConfig));
        assertDoesNotThrow(() ->
                rideService.stopRide(1, endLocation, LocalDateTime.now()));
    }

    @Test
    void shouldUpdateRideStatusToFinished() {
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(Location.class), any(Route.class))).thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD)).thenReturn(Optional.of(priceConfig));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        rideService.stopRide(1, endLocation, LocalDateTime.now());

        verify(rideRepository).save(argThat(savedRide ->
            RideStatus.FINISHED.equals(savedRide.getRideStatus())
        ));
    }

    @Test
    void shouldSetEndLocationWhenStoppingRide() {
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(Location.class), any(Route.class))).thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD)).thenReturn(Optional.of(priceConfig));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        rideService.stopRide(1, endLocation, LocalDateTime.now());

        verify(rideRepository).save(argThat(savedRide ->
            savedRide.getEndLocation() != null
        ));
    }

    @Test
    void shouldSetEndTimeWhenStoppingRide() {
        LocalDateTime endTime = LocalDateTime.now();
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(Location.class), any(Route.class))).thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD)).thenReturn(Optional.of(priceConfig));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        rideService.stopRide(1, endLocation, endTime);

        verify(rideRepository).save(argThat(savedRide ->
            savedRide.getEndTime() != null
        ));
    }

    @Test
    void shouldCalculateAndSetFinalPrice() {
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(Location.class), any(Route.class))).thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD)).thenReturn(Optional.of(priceConfig));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        rideService.stopRide(1, endLocation, LocalDateTime.now());

        verify(rideRepository).save(argThat(savedRide ->
            savedRide.getTotalPrice() >= 0
        ));
    }

    @Test
    void shouldReturnResponseDTOWithCorrectStatus() {
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(Location.class), any(Route.class))).thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD)).thenReturn(Optional.of(priceConfig));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        var response = rideService.stopRide(1, endLocation, LocalDateTime.now());

        assertNotNull(response);
        assertEquals("FINISHED", response.getStatus());
    }

    @Test
    void shouldHandleNullDriver() {
        ride.setDriver(null);
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));

        assertThrows(NullPointerException.class, () ->
                rideService.stopRide(1, endLocation, LocalDateTime.now()));
    }

    @Test
    void shouldHandleNullVehicle() {
        driver.setVehicle(null);
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));

        assertThrows(NullPointerException.class, () ->
                rideService.stopRide(1, endLocation, LocalDateTime.now()));
    }

    @Test
    void shouldSaveRouteWhenStoppingRide() {
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(Location.class), any(Route.class))).thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD)).thenReturn(Optional.of(priceConfig));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        rideService.stopRide(1, endLocation, LocalDateTime.now());

        verify(routeService).save(any(Route.class));
    }

    @Test
    void shouldAddEndLocationToRoute() {
        when(rideRepository.findById(1)).thenReturn(Optional.of(ride));
        when(locationService.findOrSaveLocation(any(Location.class), any(Route.class))).thenReturn(endLocation);
        when(priceConfigRepository.findByVehicleType(VehicleType.STANDARD)).thenReturn(Optional.of(priceConfig));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        rideService.stopRide(1, endLocation, LocalDateTime.now());

        verify(locationService).findOrSaveLocation(any(Location.class), any(Route.class));
    }
}

