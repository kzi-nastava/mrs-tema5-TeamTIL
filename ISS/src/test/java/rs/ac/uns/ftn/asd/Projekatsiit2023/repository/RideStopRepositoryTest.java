package rs.ac.uns.ftn.asd.Projekatsiit2023.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Location;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.RegisteredUser;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Route;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RideStopRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RideRepository rideRepository;

    private static int userCounter = 0;

    private Location createLocation(String address, Route route) {
        Location location = new Location();
        location.setAddress(address);
        location.setLatitude(40.7128 + Math.random());
        location.setLongitude(-74.0060 + Math.random());
        location.setRoute(route);
        entityManager.persistAndFlush(location);
        return location;
    }

    private RegisteredUser createRegisteredUser() {
        userCounter++;
        RegisteredUser user = new RegisteredUser();
        user.setEmail("test" + userCounter + "@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password");
        user.setUserType(UserType.REGISTERED_USER);
        user.setIsBlocked(false);
        entityManager.persistAndFlush(user);
        return user;
    }

    private Ride createValidRide(RideStatus status) {
        RegisteredUser passenger = createRegisteredUser();

        Route route = new Route();
        route.setDistance(10.5);
        route.setEstimatedTime(30L);
        entityManager.persistAndFlush(route);

        Location startLocation = createLocation("Start Location", route);
        Location endLocation = createLocation("End Location", route);

        Ride ride = new Ride();
        ride.setRideStatus(status);
        ride.setStartTime(LocalDateTime.now());
        ride.setTotalPrice(100.0);
        ride.setStartLocation(startLocation);
        ride.setEndLocation(endLocation);
        ride.setRoute(route);
        ride.setPassenger(passenger);

        return ride;
    }

    @BeforeEach
    void setUp() {
        // Setup per test
    }


    @Test
    void shouldFindActiveRideById() {
        Ride ride = createValidRide(RideStatus.IN_PROGRESS);
        entityManager.persistAndFlush(ride);

        Optional<Ride> found = rideRepository.findActiveRideById(ride.getId());

        assertTrue(found.isPresent());
        assertEquals(RideStatus.IN_PROGRESS, found.get().getRideStatus());
    }

    @Test
    void shouldNotFindRideIfNotInProgress() {
        Ride ride = createValidRide(RideStatus.FINISHED);
        entityManager.persistAndFlush(ride);

        Optional<Ride> found = rideRepository.findActiveRideById(ride.getId());

        assertFalse(found.isPresent());
    }

    @Test
    void shouldNotFindNonExistentRide() {
        Optional<Ride> found = rideRepository.findActiveRideById(999);

        assertFalse(found.isPresent());
    }

    // ===== GRANIČNI I IZUZETNI SLUČAJEVI =====

    @Test
    void shouldNotFindRideWithStatusRequested() {
        Ride ride = createValidRide(RideStatus.REQUESTED);
        entityManager.persistAndFlush(ride);

        Optional<Ride> found = rideRepository.findActiveRideById(ride.getId());

        assertFalse(found.isPresent(), "Ride with REQUESTED status should not be found as active");
    }

    @Test
    void shouldNotFindRideWithStatusAccepted() {
        Ride ride = createValidRide(RideStatus.ACCEPTED);
        entityManager.persistAndFlush(ride);

        Optional<Ride> found = rideRepository.findActiveRideById(ride.getId());

        assertFalse(found.isPresent(), "Ride with ACCEPTED status should not be found as active");
    }

    @Test
    void shouldFindRideWithStatusInProgress() {
        Ride ride = createValidRide(RideStatus.IN_PROGRESS);
        entityManager.persistAndFlush(ride);

        Optional<Ride> found = rideRepository.findActiveRideById(ride.getId());

        assertTrue(found.isPresent(), "Ride with IN_PROGRESS status should be found");
        assertEquals(RideStatus.IN_PROGRESS, found.get().getRideStatus());
    }

    @Test
    void shouldNotFindRideWithStatusFinished() {
        Ride ride = createValidRide(RideStatus.FINISHED);
        entityManager.persistAndFlush(ride);

        Optional<Ride> found = rideRepository.findActiveRideById(ride.getId());

        assertFalse(found.isPresent(), "Ride with FINISHED status should not be found as active");
    }

    @Test
    void shouldNotFindRideWithStatusCancelled() {
        Ride ride = createValidRide(RideStatus.CANCELED);
        entityManager.persistAndFlush(ride);

        Optional<Ride> found = rideRepository.findActiveRideById(ride.getId());

        assertFalse(found.isPresent(), "Ride with CANCELLED status should not be found as active");
    }

    @Test
    void shouldFindOnlyOneActiveRideWhenMultipleExist() {
        Ride activeRide = createValidRide(RideStatus.IN_PROGRESS);
        Ride finishedRide = createValidRide(RideStatus.FINISHED);
        Ride requestedRide = createValidRide(RideStatus.REQUESTED);

        entityManager.persistAndFlush(activeRide);
        entityManager.persistAndFlush(finishedRide);
        entityManager.persistAndFlush(requestedRide);

        Optional<Ride> found = rideRepository.findActiveRideById(activeRide.getId());

        assertTrue(found.isPresent());
        assertEquals(activeRide.getId(), found.get().getId());
        assertEquals(RideStatus.IN_PROGRESS, found.get().getRideStatus());
    }

    @Test
    void shouldNotFindRideWithZeroId() {
        Optional<Ride> found = rideRepository.findActiveRideById(0);

        assertFalse(found.isPresent());
    }

    @Test
    void shouldNotFindRideWithNegativeId() {
        Optional<Ride> found = rideRepository.findActiveRideById(-1);

        assertFalse(found.isPresent());
    }

    @Test
    void shouldFindActiveRideWithCorrectPassenger() {
        RegisteredUser passenger = createRegisteredUser();

        Route route = new Route();
        route.setDistance(10.5);
        route.setEstimatedTime(30L);
        entityManager.persistAndFlush(route);

        Location startLocation = createLocation("Start Location", route);
        Location endLocation = createLocation("End Location", route);
        entityManager.persistAndFlush(startLocation);
        entityManager.persistAndFlush(endLocation);

        Ride ride = new Ride();
        ride.setRideStatus(RideStatus.IN_PROGRESS);
        ride.setStartTime(LocalDateTime.now());
        ride.setTotalPrice(100.0);
        ride.setStartLocation(startLocation);
        ride.setEndLocation(endLocation);
        ride.setRoute(route);
        ride.setPassenger(passenger);

        entityManager.persistAndFlush(ride);

        Optional<Ride> found = rideRepository.findActiveRideById(ride.getId());

        assertTrue(found.isPresent());
        assertEquals(passenger.getId(), found.get().getPassenger().getId());
    }

    @Test
    void shouldFindActiveRideWithValidLocations() {
        Ride ride = createValidRide(RideStatus.IN_PROGRESS);
        entityManager.persistAndFlush(ride);

        Optional<Ride> found = rideRepository.findActiveRideById(ride.getId());

        assertTrue(found.isPresent());
        assertNotNull(found.get().getStartLocation());
        assertNotNull(found.get().getEndLocation());
        assertTrue(found.get().getStartLocation().getLatitude() > 0);
        assertTrue(found.get().getEndLocation().getLatitude() > 0);
    }

    @Test
    void shouldFindActiveRideWithCorrectPrice() {
        Ride ride = createValidRide(RideStatus.IN_PROGRESS);
        ride.setTotalPrice(125.50);
        entityManager.persistAndFlush(ride);

        Optional<Ride> found = rideRepository.findActiveRideById(ride.getId());

        assertTrue(found.isPresent());
        assertEquals(125.50, found.get().getTotalPrice());
    }

    @Test
    void shouldNotFindDeletedRide() {
        Ride ride = createValidRide(RideStatus.IN_PROGRESS);
        entityManager.persistAndFlush(ride);

        Integer rideId = ride.getId();

        // Soft delete or explicitly remove
        entityManager.remove(ride);
        entityManager.flush();

        Optional<Ride> found = rideRepository.findActiveRideById(rideId);

        assertFalse(found.isPresent(), "Deleted ride should not be found");
    }

    @Test
    void shouldFindActiveRideEvenWithNullDriver() {
        Ride ride = createValidRide(RideStatus.IN_PROGRESS);
        ride.setDriver(null); // Driver can be null until accepted
        entityManager.persistAndFlush(ride);

        Optional<Ride> found = rideRepository.findActiveRideById(ride.getId());

        assertTrue(found.isPresent(), "Ride without assigned driver should still be found if IN_PROGRESS");
        assertNull(found.get().getDriver());
    }
}
