package rs.ac.uns.ftn.asd.Projekatsiit2023.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RideEndRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private RideRepository rideRepository;

    private static int counter = 0;

    private RegisteredUser createPassenger() {
        counter++;
        RegisteredUser u = new RegisteredUser();
        u.setEmail("passenger" + counter + "@test.com");
        u.setPassword("pass");
        u.setFirstName("Ana");
        u.setLastName("Anic");
        u.setIsBlocked(false);
        u.setUserType(UserType.REGISTERED_USER);
        em.persistAndFlush(u);
        return u;
    }

    private Driver createDriver() {
        counter++;
        Vehicle v = new Vehicle();
        v.setModel("Golf");
        v.setType(VehicleType.STANDARD);
        v.setLicensePlate("NS-" + counter + "-AA");
        v.setCapacity(4);
        v.setBabyFriendly(false);
        v.setPetFriendly(false);
        v.setCurrentLatitude(45.26);
        v.setCurrentLongitude(19.83);
        em.persistAndFlush(v);

        Driver d = new Driver();
        d.setEmail("driver" + counter + "@test.com");
        d.setPassword("pass");
        d.setFirstName("Petar");
        d.setLastName("Petrovic");
        d.setIsBlocked(false);
        d.setIsActive(true);
        d.setActiveHours(0L);
        d.setUserType(UserType.DRIVER);
        d.setVehicle(v);
        em.persistAndFlush(d);
        return d;
    }

    private Route createRoute() {
        Route r = new Route();
        r.setDistance(10.0);
        r.setEstimatedTime(1800L);
        em.persistAndFlush(r);
        return r;
    }

    private Location createLocation(String address, Route route) {
        Location l = new Location();
        l.setAddress(address);
        l.setLatitude(45.26 + Math.random() * 0.05);
        l.setLongitude(19.83 + Math.random() * 0.05);
        l.setRoute(route);
        em.persistAndFlush(l);
        return l;
    }

    private Ride createRide(RideStatus status, RegisteredUser passenger,
                            Driver driver, Location start, Location end,
                            Route route, LocalDateTime scheduledTime) {
        Ride ride = new Ride();
        ride.setRideStatus(status);
        ride.setStartTime(scheduledTime);
        ride.setScheduledTime(scheduledTime);
        ride.setTotalPrice(1500.0);
        ride.setPassenger(passenger);
        ride.setDriver(driver);
        ride.setStartLocation(start);
        ride.setEndLocation(end);
        ride.setRoute(route);
        em.persistAndFlush(ride);
        return ride;
    }

    // Osnovan slučaj: vozač ima jednu REQUESTED vožnju, treba da je pronađe
    @Test
    void shouldFindNextRequestedRideForDriver() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route       = createRoute();
        Location start    = createLocation("Start", route);
        Location end      = createLocation("End", route);

        LocalDateTime future = LocalDateTime.now().plusHours(2);
        Ride ride = createRide(RideStatus.REQUESTED, p, driver, start, end, route, future);

        Optional<Ride> result = rideRepository.findNextRideByDriverId(
                driver.getId(), List.of(RideStatus.REQUESTED));

        assertTrue(result.isPresent());
        assertEquals(ride.getId(), result.get().getId());
    }

    // Ako nema zakazanih vožnji, vraća Optional.empty()
    @Test
    void shouldReturnEmptyWhenNoNextRideExists() {
        Driver driver = createDriver();

        Optional<Ride> result = rideRepository.findNextRideByDriverId(
                driver.getId(), List.of(RideStatus.REQUESTED));

        assertFalse(result.isPresent());
    }

    // FINISHED vožnja se ne sme pojaviti kao sledeća vožnja
    @Test
    void shouldNotReturnFinishedRideAsNext() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route       = createRoute();
        Location start    = createLocation("Start", route);
        Location end      = createLocation("End", route);

        createRide(RideStatus.FINISHED, p, driver, start, end, route,
                LocalDateTime.now().plusHours(1));

        Optional<Ride> result = rideRepository.findNextRideByDriverId(
                driver.getId(), List.of(RideStatus.REQUESTED));

        assertFalse(result.isPresent());
    }

    // IN_PROGRESS vožnja se ne sme pojaviti kao sledeća
    @Test
    void shouldNotReturnInProgressRideAsNext() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route       = createRoute();
        Location start    = createLocation("Start", route);
        Location end      = createLocation("End", route);

        createRide(RideStatus.IN_PROGRESS, p, driver, start, end, route,
                LocalDateTime.now().minusMinutes(10));

        Optional<Ride> result = rideRepository.findNextRideByDriverId(
                driver.getId(), List.of(RideStatus.REQUESTED));

        assertFalse(result.isPresent());
    }

    // CANCELED vožnja se ne sme pojaviti kao sledeća
    @Test
    void shouldNotReturnCancelledRideAsNext() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route       = createRoute();
        Location start    = createLocation("Start", route);
        Location end      = createLocation("End", route);

        createRide(RideStatus.CANCELED, p, driver, start, end, route,
                LocalDateTime.now().plusHours(1));

        Optional<Ride> result = rideRepository.findNextRideByDriverId(
                driver.getId(), List.of(RideStatus.REQUESTED));

        assertFalse(result.isPresent());
    }

    // Vozač ima dve zakazane vožnje — vraća se ona sa ranijim startTime
    @Test
    void shouldReturnEarliestNextRideWhenMultipleExist() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route1      = createRoute();
        Route route2      = createRoute();
        Location s1 = createLocation("Start1", route1);
        Location e1 = createLocation("End1",   route1);
        Location s2 = createLocation("Start2", route2);
        Location e2 = createLocation("End2",   route2);

        LocalDateTime earlier = LocalDateTime.now().plusHours(1);
        LocalDateTime later   = LocalDateTime.now().plusHours(3);

        Ride earlierRide = createRide(RideStatus.REQUESTED, p, driver, s1, e1, route1, earlier);
        createRide(RideStatus.REQUESTED, p, driver, s2, e2, route2, later);

        Optional<Ride> result = rideRepository.findNextRideByDriverId(
                driver.getId(), List.of(RideStatus.REQUESTED));

        assertTrue(result.isPresent());
        assertEquals(earlierRide.getId(), result.get().getId());
    }

    // Vožnje drugog vozača se ne smeju vraćati
    @Test
    void shouldNotReturnRidesOfOtherDrivers() {
        Driver driver1    = createDriver();
        Driver driver2    = createDriver();
        RegisteredUser p  = createPassenger();
        Route route       = createRoute();
        Location start    = createLocation("Start", route);
        Location end      = createLocation("End", route);

        // Samo driver2 ima zakazanu vožnju
        createRide(RideStatus.REQUESTED, p, driver2, start, end, route,
                LocalDateTime.now().plusHours(1));

        Optional<Ride> result = rideRepository.findNextRideByDriverId(
                driver1.getId(), List.of(RideStatus.REQUESTED));

        assertFalse(result.isPresent());
    }

    // Nepostojeci driverID — prazna lista
    @Test
    void shouldReturnEmptyForNonExistentDriverId() {
        Optional<Ride> result = rideRepository.findNextRideByDriverId(
                999999, List.of(RideStatus.REQUESTED));

        assertFalse(result.isPresent());
    }

    // Status IN_PROGRESS -> FINISHED se ispravno čuva
    @Test
    void shouldPersistStatusChangeToFinished() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route       = createRoute();
        Location start    = createLocation("Start", route);
        Location end      = createLocation("End", route);

        Ride ride = createRide(RideStatus.IN_PROGRESS, p, driver, start, end, route,
                LocalDateTime.now().minusMinutes(30));

        ride.setRideStatus(RideStatus.FINISHED);
        ride.setEndTime(LocalDateTime.now());
        rideRepository.save(ride);
        em.flush();
        em.clear();

        Optional<Ride> found = rideRepository.findById(ride.getId());
        assertTrue(found.isPresent());
        assertEquals(RideStatus.FINISHED, found.get().getRideStatus());
    }

    // Vreme završetka vožnje se ispravno čuva
    @Test
    void shouldPersistEndTimeAfterFinishing() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route       = createRoute();
        Location start    = createLocation("Start", route);
        Location end      = createLocation("End", route);

        Ride ride = createRide(RideStatus.IN_PROGRESS, p, driver, start, end, route,
                LocalDateTime.now().minusMinutes(30));

        LocalDateTime finishTime = LocalDateTime.now();
        ride.setRideStatus(RideStatus.FINISHED);
        ride.setEndTime(finishTime);
        rideRepository.save(ride);
        em.flush();
        em.clear();

        Optional<Ride> found = rideRepository.findById(ride.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getEndTime());
    }

    // Finalna cena se ispravno čuva
    @Test
    void shouldPersistFinalPriceAfterFinishing() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route       = createRoute();
        Location start    = createLocation("Start", route);
        Location end      = createLocation("End", route);

        Ride ride = createRide(RideStatus.IN_PROGRESS, p, driver, start, end, route,
                LocalDateTime.now().minusMinutes(30));

        ride.setRideStatus(RideStatus.FINISHED);
        ride.setTotalPrice(1480.0);
        rideRepository.save(ride);
        em.flush();
        em.clear();

        Optional<Ride> found = rideRepository.findById(ride.getId());
        assertTrue(found.isPresent());
        assertEquals(1480.0, found.get().getTotalPrice());
    }

    // Krajnja lokacija se ispravno čuva
    @Test
    void shouldPersistEndLocationAfterFinishing() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route       = createRoute();
        Location start    = createLocation("Start", route);
        Location end      = createLocation("End", route);

        Ride ride = createRide(RideStatus.IN_PROGRESS, p, driver, start, end, route,
                LocalDateTime.now().minusMinutes(30));

        ride.setRideStatus(RideStatus.FINISHED);
        ride.setEndLocation(end);
        rideRepository.save(ride);
        em.flush();
        em.clear();

        Optional<Ride> found = rideRepository.findById(ride.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getEndLocation());
    }

    // Nakon završetka vožnje, vozač ostaje pravilno asociran
    @Test
    void shouldPreserveDriverAssociationAfterFinishing() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route       = createRoute();
        Location start    = createLocation("Start", route);
        Location end      = createLocation("End", route);

        Ride ride = createRide(RideStatus.IN_PROGRESS, p, driver, start, end, route,
                LocalDateTime.now().minusMinutes(30));

        ride.setRideStatus(RideStatus.FINISHED);
        rideRepository.save(ride);
        em.flush();
        em.clear();

        Optional<Ride> found = rideRepository.findById(ride.getId());
        assertTrue(found.isPresent());
        assertEquals(driver.getEmail(), found.get().getDriver().getEmail());
    }

    // Putnik ostaje pravilno asociran nakon završetka
    @Test
    void shouldPreservePassengerAssociationAfterFinishing() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route       = createRoute();
        Location start    = createLocation("Start", route);
        Location end      = createLocation("End", route);

        Ride ride = createRide(RideStatus.IN_PROGRESS, p, driver, start, end, route,
                LocalDateTime.now().minusMinutes(30));

        ride.setRideStatus(RideStatus.FINISHED);
        rideRepository.save(ride);
        em.flush();
        em.clear();

        Optional<Ride> found = rideRepository.findById(ride.getId());
        assertTrue(found.isPresent());
        assertEquals(p.getEmail(), found.get().getPassenger().getEmail());
    }
}

