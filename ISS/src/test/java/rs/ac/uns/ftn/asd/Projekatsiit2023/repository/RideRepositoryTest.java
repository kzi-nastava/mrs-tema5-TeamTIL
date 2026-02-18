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

/**
 * UNIT TESTOVI za RideRepository (2.4.1 Porucivanje voznje)
 * Testiramo:
 *   - Sve custom metode repozitorijuma koje se koriste u logici porucivanja voznje
 *   - Ispravnost JPA queryja, Query anotacije
 *   - DataJpaTest ucitava samo JPA sloj odn samo repo i entitete
 *   - Perzistencija i citanje podataka iz baze
 */

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;NON_KEYWORDS=VALUE",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RideRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private RideRepository rideRepository;

    //brojac za jedinstvene email adrese
    private static int counter = 0;

    // ----------------------------------------------------------------
    // POMOCNE METODE za kreiranje testnih podataka
    // ----------------------------------------------------------------

    //kreira i cuva putnika sa jedinstvenim mejlom
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

    //kreira i cuva vozaca sa vozilom i jedinstvenim mejlom
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

    //kreira i cuva rutu
    private Route createRoute() {
        Route r = new Route();
        r.setDistance(10.0);
        r.setEstimatedTime(1800L);
        em.persistAndFlush(r);
        return r;
    }

    //kreira i cuva lokaciju vezanu za rutu
    private Location createLocation(String address, Route route) {
        Location l = new Location();
        l.setAddress(address);
        l.setLatitude(45.26 + Math.random() * 0.05);
        l.setLongitude(19.83 + Math.random() * 0.05);
        l.setRoute(route);
        em.persistAndFlush(l);
        return l;
    }

    //kreira i cuva voznju 1h unapred
    private Ride createRide(RideStatus status, RegisteredUser passenger,
                            Driver driver, Location start, Location end, Route route) {
        Ride ride = new Ride();
        ride.setRideStatus(status);
        ride.setStartTime(LocalDateTime.now().minusMinutes(30));
        ride.setScheduledTime(LocalDateTime.now().plusHours(1));
        ride.setTotalPrice(1500.0);
        ride.setPassenger(passenger);
        ride.setDriver(driver);
        ride.setStartLocation(start);
        ride.setEndLocation(end);
        ride.setRoute(route);
        em.persistAndFlush(ride);
        return ride;
    }

    // ================================================================
    // TESTOVI ZA findByDriverIdAndRideStatusIn
    // koristimo da proverimo je l vozac ima aktivnih ili zakazanih voznji
    // ================================================================

    //osnovan slucaj: vozac ima REQUESTED voznju, trazi po statusu REQUESTED i IN_PROGRESS
    @Test
    void shouldFindRidesByDriverIdAndStatusIn() {
        Driver driver = createDriver();
        RegisteredUser passenger = createPassenger();
        Route route = createRoute();
        Location start = createLocation("Start", route);
        Location end   = createLocation("End",   route);

        Ride ride = createRide(RideStatus.REQUESTED, passenger, driver, start, end, route);

        List<Ride> result = rideRepository.findByDriverIdAndRideStatusIn(
                driver.getId(), List.of(RideStatus.REQUESTED, RideStatus.IN_PROGRESS));

        assertEquals(1, result.size());
        assertEquals(ride.getId(), result.get(0).getId());
    }

    //vozac bez ikakvih aktivnih voznji, prazna lista
    @Test
    void shouldReturnEmptyWhenDriverHasNoActiveRides() {
        Driver driver = createDriver();

        List<Ride> result = rideRepository.findByDriverIdAndRideStatusIn(
                driver.getId(), List.of(RideStatus.REQUESTED, RideStatus.IN_PROGRESS));

        assertTrue(result.isEmpty());
    }

    //FINISHED voznja ne sme biti u rezultatu kada trazimo aktivne
    @Test
    void shouldNotReturnFinishedRidesForDriver() {
        Driver driver = createDriver();
        RegisteredUser passenger = createPassenger();
        Route route = createRoute();
        Location start = createLocation("Start", route);
        Location end   = createLocation("End",   route);

        createRide(RideStatus.FINISHED, passenger, driver, start, end, route);

        List<Ride> result = rideRepository.findByDriverIdAndRideStatusIn(
                driver.getId(), List.of(RideStatus.REQUESTED, RideStatus.IN_PROGRESS));

        assertTrue(result.isEmpty());
    }

    //isto i CANCELED voznja
    @Test
    void shouldNotReturnCanceledRidesForDriver() {
        Driver driver = createDriver();
        RegisteredUser passenger = createPassenger();
        Route route = createRoute();
        Location start = createLocation("Start", route);
        Location end   = createLocation("End",   route);

        createRide(RideStatus.CANCELED, passenger, driver, start, end, route);

        List<Ride> result = rideRepository.findByDriverIdAndRideStatusIn(
                driver.getId(), List.of(RideStatus.REQUESTED, RideStatus.IN_PROGRESS));

        assertTrue(result.isEmpty());
    }

    // ================================================================
    // TESTOVI ZA findById
    // ================================================================

    //postoji voznja
    @Test
    void shouldFindRideById() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route = createRoute();
        Location start = createLocation("Start", route);
        Location end   = createLocation("End",   route);

        Ride ride = createRide(RideStatus.REQUESTED, p, driver, start, end, route);

        Optional<Ride> found = rideRepository.findById(ride.getId());

        assertTrue(found.isPresent());
        assertEquals(ride.getId(), found.get().getId());
    }

    //ne postoji voznja sa tim idjem
    @Test
    void shouldReturnEmptyWhenRideDoesNotExist() {
        Optional<Ride> found = rideRepository.findById(999999);
        assertFalse(found.isPresent());
    }

    //negativan id ne sme da ima nista
    @Test
    void shouldReturnEmptyForNegativeId() {
        Optional<Ride> found = rideRepository.findById(-1);
        assertFalse(found.isPresent());
    }

    // ================================================================
    // TESTOVI ya save i perzistenciju
    // provera da se podaci ispravno cuvaju i citaju
    // ================================================================

    //kreirana voznja ima ispravan status u bazi
    @Test
    void shouldPersistRideWithCorrectStatus() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route = createRoute();
        Location start = createLocation("Start", route);
        Location end   = createLocation("End",   route);

        Ride ride = createRide(RideStatus.REQUESTED, p, driver, start, end, route);

        Optional<Ride> found = rideRepository.findById(ride.getId());
        assertTrue(found.isPresent());
        assertEquals(RideStatus.REQUESTED, found.get().getRideStatus());
    }

    //kreirana voznja ima ispravnog putnika u bazi
    @Test
    void shouldPersistRideWithCorrectPassenger() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route = createRoute();
        Location start = createLocation("Start", route);
        Location end   = createLocation("End",   route);

        Ride ride = createRide(RideStatus.REQUESTED, p, driver, start, end, route);

        Optional<Ride> found = rideRepository.findById(ride.getId());
        assertTrue(found.isPresent());
        assertEquals(p.getEmail(), found.get().getPassenger().getEmail());
    }

    //kreirana voznja ima ispravnog vozaca u bazi
    @Test
    void shouldPersistRideWithCorrectDriver() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route = createRoute();
        Location start = createLocation("Start", route);
        Location end   = createLocation("End",   route);

        Ride ride = createRide(RideStatus.REQUESTED, p, driver, start, end, route);

        Optional<Ride> found = rideRepository.findById(ride.getId());
        assertTrue(found.isPresent());
        assertEquals(driver.getEmail(), found.get().getDriver().getEmail());
    }

    //cena se ispravno cuva i cita
    @Test
    void shouldPersistRideWithCorrectPrice() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route = createRoute();
        Location start = createLocation("Start", route);
        Location end   = createLocation("End",   route);

        Ride ride = createRide(RideStatus.REQUESTED, p, driver, start, end, route);

        Optional<Ride> found = rideRepository.findById(ride.getId());
        assertTrue(found.isPresent());
        assertEquals(1500.0, found.get().getTotalPrice());
    }

    //azuriranje statusa voznje sa REQUESTED na IN_PROGRESS
    @Test
    void shouldUpdateRideStatusAfterSave() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route = createRoute();
        Location start = createLocation("Start", route);
        Location end   = createLocation("End",   route);

        Ride ride = createRide(RideStatus.REQUESTED, p, driver, start, end, route);
        ride.setRideStatus(RideStatus.IN_PROGRESS);
        rideRepository.save(ride);
        em.flush();
        em.clear(); // brise JPA cache — sledece citanje ide direktno iz baze

        Optional<Ride> found = rideRepository.findById(ride.getId());
        assertTrue(found.isPresent());
        assertEquals(RideStatus.IN_PROGRESS, found.get().getRideStatus());
    }

    //zakazana voznja se ispravno cuva i moze se naci za vozaca
    @Test
    void shouldFindScheduledRideForDriver() {
        Driver driver     = createDriver();
        RegisteredUser p  = createPassenger();
        Route route = createRoute();
        Location start = createLocation("Start", route);
        Location end   = createLocation("End",   route);

        Ride ride = createRide(RideStatus.REQUESTED, p, driver, start, end, route);
        ride.setScheduledTime(LocalDateTime.now().plusHours(2));
        rideRepository.save(ride);
        em.flush();

        List<Ride> result = rideRepository.findByDriverIdAndRideStatusIn(
                driver.getId(), List.of(RideStatus.REQUESTED));

        assertFalse(result.isEmpty());
        assertNotNull(result.get(0).getScheduledTime());
    }
}