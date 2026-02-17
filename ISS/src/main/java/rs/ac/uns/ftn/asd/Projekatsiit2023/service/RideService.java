package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.AssignedRideDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.LinkdPassengerDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.DriverRideDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.RideRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Locale;
import java.time.format.DateTimeFormatter;

@Service
public class RideService {
    @Autowired
    private RouteService routeService;

    @Autowired
    private RegisteredUserRepository userRepository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private PriceConfigRepository priceConfigRepository;
    @Autowired
    private PanicNotificationRepository panicNotificationRepository;
    @Autowired
    private DriverRepository driverRepository;

    private final RideRepository rideRepository;
    private static final Logger logger = LoggerFactory.getLogger(RideService.class);

    public RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @Transactional
    public RideCancelResponseDTO cancelRide(Integer rideId, String reason) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));

        if (RideStatus.CANCELED.equals(ride.getRideStatus()) || RideStatus.FINISHED.equals(ride.getRideStatus())) {
            throw new RuntimeException("Cannot cancel ride with status: " + ride.getRideStatus());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isRegisteredUser = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_REGISTERED_USER"));
        if (isRegisteredUser) {
            if (ride.getScheduledTime().isAfter(LocalDateTime.now().minusMinutes(10))) {
                throw new RuntimeException("Rides can’t be canceled less than 10 minutes before they start.");
            }
            ride.setCancellationReason("User cancelled");
            ride.setCanceledBy(UserType.REGISTERED_USER);
        }
        else {
            if (reason == null) {
                throw new RuntimeException("Driver must enter cancellation reason.");
            }
            ride.setCancellationReason(reason);
            ride.setCanceledBy(UserType.DRIVER);
        }
        ride.setRideStatus(RideStatus.CANCELED);
        Ride savedRide = rideRepository.save(ride);

        return new RideCancelResponseDTO(
                savedRide.getId(),
                savedRide.getRideStatus().name(),
                savedRide.getCancellationReason(),
                "Ride cancelled successfully",
                savedRide.getCanceledBy() != null ? savedRide.getCanceledBy().name() : null
        );
    }

    @Transactional
    public RideStartResponseDTO startRide(Integer rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));

        if (!RideStatus.REQUESTED.equals(ride.getRideStatus())) {
            throw new RuntimeException("Ride cannot be started. Current status: " + ride.getRideStatus());
        }

        ride.setRideStatus(RideStatus.IN_PROGRESS);
        ride.setStartTime(LocalDateTime.now());
        Ride savedRide = rideRepository.save(ride);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        return new RideStartResponseDTO(
                savedRide.getId(),
                savedRide.getRideStatus().name(),
                "Ride started successfully",
                savedRide.getStartTime().format(formatter)
        );
    }

    public AssignedRideDTO mapRideToDTO(Ride ride, UserType userType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

        // Estimate route
        RouteService.RouteEstimation estimation = routeService.estimateRoute(
                ride.getStartLocation().getLatitude(),
                ride.getStartLocation().getLongitude(),
                ride.getEndLocation().getLatitude(),
                ride.getEndLocation().getLongitude()
        );

        double distance = estimation != null ? estimation.distanceKm() : 0.0;
        double duration = estimation != null ? estimation.durationMin() : 0.0;

        String estimatedEndTime = null;
        if (ride.getStartTime() != null && duration > 0) {
            estimatedEndTime = ride.getStartTime().plusMinutes((long) duration).format(formatter);
        }

        double price = calculateFinalPrice(ride.getDriver().getVehicle().getType(), ride.getStartLocation(), ride.getEndLocation());

        String accountEmail = userType == UserType.DRIVER
                ? ride.getDriver().getEmail()
                : ride.getPassenger().getEmail();

        return new AssignedRideDTO(
                ride.getId(),
                accountEmail,
                ride.getStartLocation().getAddress(),
                ride.getEndLocation() != null ? ride.getEndLocation().getAddress() : "",
                ride.getRideStatus().toString(),
                ride.getStartTime() != null ? ride.getStartTime().format(formatter) : null,
                estimatedEndTime,
                price,
                distance,
                duration
        );
    }

    public List<RideHistoryResponseDTO> getAllRidesWithPanicInfoForAdmin() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        List<RideStatus> statuses = List.of(RideStatus.FINISHED, RideStatus.CANCELED);
        List<Ride> rides = rideRepository.findByRideStatusIn(statuses);

        return rides.stream().map(ride -> {
            boolean panicSent = panicNotificationRepository.existsByRideId(ride.getId());
            boolean rated = ride.getRating() != null;

            // Route estimation
            RouteService.RouteEstimation estimation = routeService.estimateRoute(
                    ride.getStartLocation().getLatitude(),
                    ride.getStartLocation().getLongitude(),
                    ride.getEndLocation().getLatitude(),
                    ride.getEndLocation().getLongitude()
            );
            double distance = estimation != null ? estimation.distanceKm() : 0.0;
            double duration = estimation != null ? estimation.durationMin() : 0.0;

            String estimatedEndTime = null;
            if (ride.getStartTime() != null && duration > 0) {
                estimatedEndTime = ride.getStartTime().plusMinutes((long) duration).format(formatter);
            }

            double price = calculateFinalPrice(ride.getDriver().getVehicle().getType(), ride.getStartLocation(), ride.getEndLocation());

            return new RideHistoryResponseDTO(
                    ride.getId(),
                    ride.getRoute() != null ? ride.getRoute().getId() : null, // DODAJ OVO
                    ride.getPassenger().getEmail(),
                    ride.getPassenger().getFirstName(),
                    ride.getPassenger().getLastName(),
                    ride.getPassenger().getProfilePictureUrl(),
                    ride.getPassenger().getPhoneNumber(),
                    ride.getDriver().getEmail(),
                    ride.getDriver().getFirstName(),
                    ride.getDriver().getLastName(),
                    ride.getDriver().getProfilePictureUrl(),
                    ride.getDriver().getPhoneNumber(),
                    ride.getStartLocation().getAddress(),
                    ride.getEndLocation() != null ? ride.getEndLocation().getAddress() : "",
                    ride.getRideStatus().toString(),
                    ride.getStartTime() != null ? ride.getStartTime().format(formatter) : null,
                    estimatedEndTime,
                    price,
                    distance,
                    duration,
                    panicSent,
                    rated,
                    ride.getDriver().getVehicle().getModel(),
                    ride.getDriver().getVehicle().getLicensePlate()
            );
        }).collect(Collectors.toList());
    }

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    public DriverRideDTO mapRideToDriverRideDTO(Ride ride) {
        List<DriverRideDTO.PassengerDTO> passengers = new ArrayList<>();
        if (ride.getPassenger() != null) {
            passengers.add(new DriverRideDTO.PassengerDTO(
                    ride.getPassenger().getFirstName() + " " + ride.getPassenger().getLastName(),
                    ride.getPassenger().getPhoneNumber()
            ));
        }
        if (ride.getCoPassengers() != null) {
            ride.getCoPassengers().forEach(p ->
                    passengers.add(new DriverRideDTO.PassengerDTO(
                            p.getFirstName() + " " + p.getLastName(),
                            p.getPhoneNumber()
                    ))
            );
        }

        String canceledBy = null;
        if (ride.getRideStatus() == RideStatus.CANCELED && ride.getCanceledBy() != null) {
            canceledBy = ride.getCanceledBy().name(); // "Driver" | "Passenger"
        }

        return new DriverRideDTO(
                ride.getId(),
                passengers,
                ride.getStartLocation().getAddress(),
                ride.getEndLocation().getAddress(),
                ride.getRideStatus() == RideStatus.FINISHED ? "Completed" : "Canceled",
                canceledBy,
                ride.getStartTime().format(DATE_FORMAT),
                ride.getStartTime().format(TIME_FORMAT),
                ride.getEndTime() != null ? ride.getEndTime().format(TIME_FORMAT) : null,
                String.format("%,.0f RSD", ride.getTotalPrice()), // "1,480 RSD"
                ride.getDurationMinutes() + " min",
                ride.getDistanceKm() + " km",
                !ride.getPanicNotifications().isEmpty()
        );
    }

    public double calculateFinalPrice(VehicleType vehicleType, Location start, Location end) {
        PriceConfig priceConfig = priceConfigRepository.findByVehicleType(vehicleType)
                .orElseThrow(() -> new RuntimeException("Price config not found"));

        double distanceKm = calculateDistanceKm(start, end);
        return priceConfig.getBasePrice() + distanceKm * priceConfig.getPricePerKm();
    }

    public double calculateDistanceKm(Location start, Location end) {
        RouteService.RouteEstimation estimation = routeService.estimateRoute(
                start.getLatitude(), start.getLongitude(),
                end.getLatitude(), end.getLongitude()
        );
        return estimation != null ? estimation.distanceKm() : 0.0;
    }

    @Transactional
    public RideCreatedResponseDTO createNewRide(RideRequestDTO request, String currentUserEmail) {
        // VALIDACIJA
        if (request.getLocations() == null || request.getLocations().size() < 2) {
            throw new RuntimeException("At least start and end location are required");
        }

        // VALIDACIJA ULINKOVANIH PUTNIKA
        if (request.getPassengerEmails() != null && !request.getPassengerEmails().isEmpty()) {
            List<String> notFoundEmails = new ArrayList<>();

            for (String email : request.getPassengerEmails()) {
                Optional<RegisteredUser> coPassengerOpt = userRepository.findByEmail(email);

                if (!coPassengerOpt.isPresent()) {
                    notFoundEmails.add(email);
                }
            }

            if (!notFoundEmails.isEmpty()) {
                String errorMsg = "The following passengers are not registered users: " +
                        String.join(", ", notFoundEmails) +
                        ". Only registered users can be added as co-passengers.";

                logger.error("VALIDATION FAILED: {}", errorMsg);
                throw new RuntimeException(errorMsg);
            }
        }

        // PRONADJI PUTNIKA
        RegisteredUser passenger = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Passenger not found: " + currentUserEmail));

        // PROVERA DA LI JE KORISNIK BLOKIRAN
        if (Boolean.TRUE.equals(passenger.getIsBlocked())) {
            String reason = passenger.getBlockReason() != null ? passenger.getBlockReason() : "No reason provided";
            throw new RuntimeException("Your account has been blocked. Reason: " + reason);
        }

        // UZMI START I END
        RideRequestDTO.LocationDTO start = request.getLocations().get(0);
        RideRequestDTO.LocationDTO end = request.getLocations().get(request.getLocations().size() - 1);

        // RACUNANJE RUTE
        RouteService.RouteEstimation estimation = routeService.estimateRoute(
                start.getLatitude(), start.getLongitude(),
                end.getLatitude(), end.getLongitude()
        );

        if (estimation == null) {
            throw new RuntimeException("Could not estimate route");
        }

        // KREIRAJ RUTU
        Route route = new Route();
        route.setDistance(estimation.distanceKm());
        route.setEstimatedTime((long) (estimation.durationMin() * 60));
        route.setLocations(new ArrayList<>());
        route = routeService.save(route);

        // DODAJ LOKACIJE U RUTU
        int order = 0;
        for (RideRequestDTO.LocationDTO locDTO : request.getLocations()) {
            Location location = new Location();
            location.setAddress(locDTO.getAddress());
            location.setLatitude(locDTO.getLatitude());
            location.setLongitude(locDTO.getLongitude());
            location.setRoute(route);
            location.setOrder(order++);

            Location savedLocation = locationService.saveLocation(location);
            route.getLocations().add(savedLocation);
        }

        // PARSE VEHICLE TYPE
        VehicleType vehicleType;
        try {
            vehicleType = VehicleType.valueOf(request.getVehicleType().toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid vehicle type: " + request.getVehicleType());
        }

        // IZRACUNAJ CENU
        double price = calculateFinalPrice(vehicleType, route.getLocations().get(0),
                route.getLocations().get(route.getLocations().size() - 1));

        // PRONADJI VOZACA
        Driver assignedDriver = findBestAvailableDriver(
                start.getLatitude(), start.getLongitude(),
                vehicleType,
                request.getBabyFriendly() != null ? request.getBabyFriendly() : false,
                request.getPetFriendly() != null ? request.getPetFriendly() : false,
                request.getScheduledTime()
        );

        if (assignedDriver == null) {
            throw new RuntimeException("No available drivers at the moment");
        }

        // PARSE SCHEDULED TIME
        LocalDateTime scheduledTime = LocalDateTime.now();
        if (request.getScheduledTime() != null && !request.getScheduledTime().isEmpty()) {
            try {
                scheduledTime = LocalDateTime.parse(request.getScheduledTime());
            } catch (Exception e) {
                logger.error("Error parsing scheduled time: {}", e.getMessage());
            }
        }

        // KREIRAJ VOZNJU
        Ride ride = new Ride();
        ride.setPassenger(passenger);
        ride.setDriver(assignedDriver);
        ride.setRoute(route);
        ride.setStartLocation(route.getLocations().get(0));
        ride.setEndLocation(route.getLocations().get(route.getLocations().size() - 1));
        ride.setRideStatus(RideStatus.REQUESTED);
        ride.setScheduledTime(scheduledTime);
        ride.setStartTime(scheduledTime);
        ride.setTotalPrice(price);
        ride.setCoPassengers(new ArrayList<>());

        // DODAJ VALIDATED CO-PASSENGERS
        if (request.getPassengerEmails() != null && !request.getPassengerEmails().isEmpty()) {
            for (String email : request.getPassengerEmails()) {
                userRepository.findByEmail(email).ifPresent(coPassenger -> {
                    ride.getCoPassengers().add(coPassenger);
                });
            }
        }

        // SACUVAJ
        Ride savedRide = rideRepository.save(ride);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        String estimatedEndTime = scheduledTime.plusMinutes((long) estimation.durationMin()).format(formatter);

        return new RideCreatedResponseDTO(
                savedRide.getId(),
                "REQUESTED",
                price,
                assignedDriver.getFirstName() + " " + assignedDriver.getLastName(),
                assignedDriver.getEmail(),
                assignedDriver.getVehicle().getModel() + " (" + assignedDriver.getVehicle().getType() + ")",
                "Ride successfully created and assigned to driver",
                scheduledTime.format(formatter),
                estimatedEndTime,
                estimation.distanceKm(),
                estimation.durationMin()
        );
    }

    public RideDetailsResponseDTO mapRideToDetailsDTO(Ride ride) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

        // Map route locations to LocationResponseDTO
        List<LocationResponseDTO> routeLocations = new ArrayList<>();
        if (ride.getRoute() != null && ride.getRoute().getLocations() != null) {
            routeLocations = ride.getRoute().getLocations().stream()
                    .filter(location -> location != null)
                    .map(location -> new LocationResponseDTO(
                            location.getAddress(),
                            String.valueOf(location.getLatitude()),
                            String.valueOf(location.getLongitude())
                    ))
                    .collect(Collectors.toList());
        }

        // For FINISHED or CANCELED rides, use data from database
        if (ride.getRideStatus() == RideStatus.FINISHED || ride.getRideStatus() == RideStatus.CANCELED) {
            // Use actual values from database
            String endTimeFormatted = ride.getEndTime() != null ? ride.getEndTime().format(formatter) : null;
            double price = ride.getTotalPrice() != null ? ride.getTotalPrice() : 0.0;
            double distance = ride.getDistanceKm();
            double duration = ride.getDurationMinutes();

            assert ride.getEndLocation() != null;
            return new RideDetailsResponseDTO(
                    ride.getId(),
                    ride.getPassenger().getFirstName(),
                    ride.getPassenger().getLastName(),
                    ride.getPassenger().getProfilePictureUrl(),
                    ride.getPassenger().getPhoneNumber(),
                    ride.getDriver() != null ? ride.getDriver().getFirstName() : null,
                    ride.getDriver() != null ? ride.getDriver().getLastName() : null,
                    ride.getDriver() != null ? ride.getDriver().getProfilePictureUrl() : null,
                    ride.getDriver() != null ? ride.getDriver().getPhoneNumber() : null,
                    ride.getDriver() != null ? ride.getDriver().getAverageRating() : null,
                    routeLocations,
                    (ride.getCoPassengers() != null) ?
                            ride.getCoPassengers().stream()
                                    .map(co -> new LinkdPassengerDTO(
                                            co.getFirstName(), co.getLastName(), co.getProfilePictureUrl()))
                                    .collect(Collectors.toList())
                            : new ArrayList<>(),
                    ride.getRideStatus().name(),
                    ride.getStartTime() != null ? ride.getStartTime().format(formatter) : null,
                    endTimeFormatted,
                    Double.valueOf(price),
                    Double.valueOf(distance),
                    Double.valueOf(duration),
                    ride.getRating() != null ? ride.getRating().getOverallRating() : null,
                    ride.getRating() != null ? ride.getRating().getComment() : null,
                    ride.getPanicNotifications() != null && !ride.getPanicNotifications().isEmpty(),
                    ride.getInconsistencyReports() != null ?
                            ride.getInconsistencyReports().stream()
                                    .map(InconsistencyReport::getDescription)
                                    .collect(Collectors.toList())
                            : new ArrayList<>()
            );
        }

        // For REQUESTED or IN_PROGRESS rides, estimate the values
        RouteService.RouteEstimation estimation = routeService.estimateRoute(
                ride.getStartLocation().getLatitude(),
                ride.getStartLocation().getLongitude(),
                ride.getEndLocation().getLatitude(),
                ride.getEndLocation().getLongitude()
        );
        double distance = estimation != null ? estimation.distanceKm() : 0.0;
        double duration = estimation != null ? estimation.durationMin() : 0.0;

        // Calculate estimated end time
        String endTimeFormatted = null;
        if (ride.getStartTime() != null && duration > 0) {
            endTimeFormatted = ride.getStartTime().plusMinutes((long) duration).format(formatter);
        }

        // Calculate estimated price
        double price = calculateFinalPrice(
                ride.getDriver() != null ? ride.getDriver().getVehicle().getType() : VehicleType.STANDARD,
                ride.getStartLocation(),
                ride.getEndLocation()
        );

        assert ride.getEndLocation() != null;
        return new RideDetailsResponseDTO(
                ride.getId(),
                ride.getPassenger().getFirstName(),
                ride.getPassenger().getLastName(),
                ride.getPassenger().getProfilePictureUrl(),
                ride.getPassenger().getPhoneNumber(),
                ride.getDriver() != null ? ride.getDriver().getFirstName() : null,
                ride.getDriver() != null ? ride.getDriver().getLastName() : null,
                ride.getDriver() != null ? ride.getDriver().getProfilePictureUrl() : null,
                ride.getDriver() != null ? ride.getDriver().getPhoneNumber() : null,
                ride.getDriver() != null ? ride.getDriver().getAverageRating() : null,
                routeLocations,
                (ride.getCoPassengers() != null) ?
                        ride.getCoPassengers().stream()
                                .map(co -> new LinkdPassengerDTO(
                                        co.getFirstName(), co.getLastName(), co.getProfilePictureUrl()))
                                .collect(Collectors.toList())
                        : new ArrayList<>(),
                ride.getRideStatus().name(),
                ride.getStartTime() != null ? ride.getStartTime().format(formatter) : null,
                endTimeFormatted,
                Double.valueOf(price),
                Double.valueOf(distance),
                Double.valueOf(duration),
                ride.getRating() != null ? ride.getRating().getOverallRating() : null,
                ride.getRating() != null ? ride.getRating().getComment() : null,
                ride.getPanicNotifications() != null && !ride.getPanicNotifications().isEmpty(),
                ride.getInconsistencyReports() != null ?
                        ride.getInconsistencyReports().stream()
                                .map(InconsistencyReport::getDescription)
                                .collect(Collectors.toList())
                        : new ArrayList<>()
        );
    }

    @Transactional
    public RideStopResponseDTO stopRide(Integer rideId, Location actualEndLocation, LocalDateTime actualEndTime) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        if (RideStatus.FINISHED.equals(ride.getRideStatus())) {
            throw new RuntimeException("Ride is already finished");
        }
        Route route = ride.getRoute();
        VehicleType vehicleType = ride.getDriver().getVehicle().getType();
        double finalPrice = calculateFinalPrice(
                vehicleType,
                ride.getStartLocation(),
                actualEndLocation
        );
        actualEndLocation.setRoute(route);
        Location endLocation = locationService.findOrSaveLocation(actualEndLocation, route);
        if (endLocation != null) {
            route.getLocations().add(endLocation);
        }
        routeService.save(route);
        ride.setRideStatus(RideStatus.FINISHED);
        ride.setEndLocation(endLocation);
        ride.setEndTime(actualEndTime);
        ride.setTotalPrice(finalPrice);
        ride.setRoute(route);
        rideRepository.save(ride);
        long durationMinutes = 0;
        if (ride.getStartTime() != null && ride.getEndTime() != null) {
            durationMinutes = ChronoUnit.MINUTES.between(ride.getStartTime(), ride.getEndTime());
        }
        return new RideStopResponseDTO(
                rideId,
                "FINISHED",
                endLocation != null ? endLocation.getAddress() : "",
                Math.round(finalPrice * 100.0) / 100.0,
                durationMinutes + " min",
                "Ride finished successfully"
        );
    }

    public List<DriverRideDTO> getDriverRideHistory(String driverEmail, LocalDateTime dateFrom, LocalDateTime dateTo) {
        List<RideStatus> statuses = List.of(RideStatus.FINISHED, RideStatus.CANCELED);
        List<Ride> rides = rideRepository.findByDriver_EmailAndRideStatusIn(driverEmail, statuses);

        return rides.stream()
                .filter(ride -> {
                    LocalDateTime rideDate = ride.getStartTime();

                    boolean afterFrom = dateFrom == null || !rideDate.isBefore(dateFrom);
                    boolean beforeTo  = dateTo == null || !rideDate.isAfter(dateTo);

                    return afterFrom && beforeTo;
                })
                .sorted((r1, r2) -> r2.getStartTime().compareTo(r1.getStartTime()))
                .map(this::mapRideToDriverRideDTO)
                .toList();
    }

    public List<RideHistoryResponseDTO> getUserRideHistory(String userEmail, LocalDateTime dateFrom, LocalDateTime dateTo, String sortBy, String sortDirection) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        List<RideStatus> statuses = List.of(RideStatus.FINISHED, RideStatus.CANCELED);
        List<Ride> rides = rideRepository.findByPassenger_EmailAndRideStatusIn(userEmail, statuses);

        // Filtering by date
        if (dateFrom != null || dateTo != null) {
            rides = rides.stream()
                    .filter(ride -> {
                        LocalDateTime rideDate = ride.getStartTime();
                        boolean afterFrom = dateFrom == null || !rideDate.isBefore(dateFrom);
                        boolean beforeTo = dateTo == null || !rideDate.isAfter(dateTo);
                        return afterFrom && beforeTo;
                    })
                    .collect(Collectors.toList());
        }

        // Sorting
        boolean isAscending = "asc".equalsIgnoreCase(sortDirection);

        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "date":
                    rides.sort((r1, r2) -> isAscending
                        ? r1.getStartTime().compareTo(r2.getStartTime())
                        : r2.getStartTime().compareTo(r1.getStartTime()));
                    break;
                case "price":
                    rides.sort((r1, r2) -> isAscending
                        ? Double.compare(r1.getTotalPrice() != null ? r1.getTotalPrice() : 0, r2.getTotalPrice() != null ? r2.getTotalPrice() : 0)
                        : Double.compare(r2.getTotalPrice() != null ? r2.getTotalPrice() : 0, r1.getTotalPrice() != null ? r1.getTotalPrice() : 0));
                    break;
                case "distance":
                    rides.sort((r1, r2) -> isAscending
                        ? Double.compare(r1.getDistanceKm(), r2.getDistanceKm())
                        : Double.compare(r2.getDistanceKm(), r1.getDistanceKm()));
                    break;
                case "duration":
                    rides.sort((r1, r2) -> isAscending
                        ? Long.compare(r1.getDurationMinutes(), r2.getDurationMinutes())
                        : Long.compare(r2.getDurationMinutes(), r1.getDurationMinutes()));
                    break;
                default:
                    rides.sort((r1, r2) -> r2.getStartTime().compareTo(r1.getStartTime()));
            }
        } else {
            rides.sort((r1, r2) -> r2.getStartTime().compareTo(r1.getStartTime()));
        }

        return rides.stream().map(ride -> {
            boolean panicSent = panicNotificationRepository.existsByRideId(ride.getId());
            boolean rated = ride.getRating() != null;

            return new RideHistoryResponseDTO(
                    ride.getId(),
                    ride.getRoute() != null ? ride.getRoute().getId() : null,
                    ride.getPassenger().getEmail(),
                    ride.getPassenger().getFirstName(),
                    ride.getPassenger().getLastName(),
                    ride.getPassenger().getProfilePictureUrl(),
                    ride.getPassenger().getPhoneNumber(),
                    ride.getDriver().getEmail(),
                    ride.getDriver().getFirstName(),
                    ride.getDriver().getLastName(),
                    ride.getDriver().getProfilePictureUrl(),
                    ride.getDriver().getPhoneNumber(),
                    ride.getStartLocation().getAddress(),
                    ride.getEndLocation() != null ? ride.getEndLocation().getAddress() : "",
                    ride.getRideStatus().toString(),
                    ride.getStartTime() != null ? ride.getStartTime().format(formatter) : null,
                    ride.getEndTime() != null ? ride.getEndTime().format(formatter) : null,
                    ride.getTotalPrice(),
                    ((double) ride.getDistanceKm()),
                    ((double) ride.getDurationMinutes()),
                    panicSent,
                    rated,
                    ride.getDriver().getVehicle().getModel(),
                    ride.getDriver().getVehicle().getLicensePlate()
            );
        }).collect(Collectors.toList());
    }

    public List<RideHistoryResponseDTO> getAdminRideHistory(LocalDateTime dateFrom, LocalDateTime dateTo, String sortBy, String sortDirection) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
        List<RideStatus> statuses = List.of(RideStatus.FINISHED, RideStatus.CANCELED);
        List<Ride> rides = rideRepository.findByRideStatusIn(statuses);

        // Filtering by date
        if (dateFrom != null || dateTo != null) {
            rides = rides.stream()
                    .filter(ride -> {
                        LocalDateTime rideDate = ride.getStartTime();
                        boolean afterFrom = dateFrom == null || !rideDate.isBefore(dateFrom);
                        boolean beforeTo = dateTo == null || !rideDate.isAfter(dateTo);
                        return afterFrom && beforeTo;
                    })
                    .collect(Collectors.toList());
        }

        // Sorting
        boolean isAscending = "asc".equalsIgnoreCase(sortDirection);

        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "date":
                    rides.sort((r1, r2) -> isAscending
                        ? r1.getStartTime().compareTo(r2.getStartTime())
                        : r2.getStartTime().compareTo(r1.getStartTime()));
                    break;
                case "price":
                    rides.sort((r1, r2) -> isAscending
                        ? Double.compare(r1.getTotalPrice() != null ? r1.getTotalPrice() : 0, r2.getTotalPrice() != null ? r2.getTotalPrice() : 0)
                        : Double.compare(r2.getTotalPrice() != null ? r2.getTotalPrice() : 0, r1.getTotalPrice() != null ? r1.getTotalPrice() : 0));
                    break;
                case "distance":
                    rides.sort((r1, r2) -> isAscending
                        ? Double.compare(r1.getDistanceKm(), r2.getDistanceKm())
                        : Double.compare(r2.getDistanceKm(), r1.getDistanceKm()));
                    break;
                case "duration":
                    rides.sort((r1, r2) -> isAscending
                        ? Long.compare(r1.getDurationMinutes(), r2.getDurationMinutes())
                        : Long.compare(r2.getDurationMinutes(), r1.getDurationMinutes()));
                    break;
                default:
                    rides.sort((r1, r2) -> r2.getStartTime().compareTo(r1.getStartTime()));
            }
        } else {
            rides.sort((r1, r2) -> r2.getStartTime().compareTo(r1.getStartTime()));
        }

        return rides.stream().map(ride -> {
            boolean panicSent = panicNotificationRepository.existsByRideId(ride.getId());
            boolean rated = ride.getRating() != null;

            return new RideHistoryResponseDTO(
                    ride.getId(),
                    ride.getRoute() != null ? ride.getRoute().getId() : null, // DODAJ OVO
                    ride.getPassenger().getEmail(),
                    ride.getPassenger().getFirstName(),
                    ride.getPassenger().getLastName(),
                    ride.getPassenger().getProfilePictureUrl(),
                    ride.getPassenger().getPhoneNumber(),
                    ride.getDriver().getEmail(),
                    ride.getDriver().getFirstName(),
                    ride.getDriver().getLastName(),
                    ride.getDriver().getProfilePictureUrl(),
                    ride.getDriver().getPhoneNumber(),
                    ride.getStartLocation().getAddress(),
                    ride.getEndLocation() != null ? ride.getEndLocation().getAddress() : "",
                    ride.getRideStatus().toString(),
                    ride.getStartTime() != null ? ride.getStartTime().format(formatter) : null,
                    ride.getEndTime() != null ? ride.getEndTime().format(formatter) : null,
                    ride.getTotalPrice(),
                    ((double) ride.getDistanceKm()),
                    ((double) ride.getDurationMinutes()),
                    panicSent,
                    rated,
                    ride.getDriver().getVehicle().getModel(),
                    ride.getDriver().getVehicle().getLicensePlate()
            );
        }).collect(Collectors.toList());
    }
  
    public List<double[]> getRouteForRide(Integer rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        return routeService.getRouteFromOSRM(
                ride.getStartLocation().getLatitude(),
                ride.getStartLocation().getLongitude(),
                ride.getEndLocation().getLatitude(),
                ride.getEndLocation().getLongitude()
        );
    }

    private Driver findBestAvailableDriver(
            double startLat, double startLon,
            VehicleType vehicleType,
            boolean needsBabyFriendly,
            boolean needsPetFriendly,
            String scheduledTimeStr
    ) {
        LocalDateTime scheduledTime = LocalDateTime.now();
        if (scheduledTimeStr != null && !scheduledTimeStr.isEmpty()) {
            try {
                scheduledTime = LocalDateTime.parse(scheduledTimeStr);
            } catch (Exception e) {
                logger.warn("Could not parse scheduled time, using current time");
            }
        }

        List<Driver> allDrivers = driverRepository.findAll();
        Driver bestDriver = null;
        double bestScore = -1;

        for (Driver driver : allDrivers) {
            double score = calculateDriverScore(driver, startLat, startLon, vehicleType,
                    needsBabyFriendly, needsPetFriendly, scheduledTime);

            if (score > bestScore) {
                bestScore = score;
                bestDriver = driver;
            }
        }

        return bestDriver;
    }

    private double calculateDriverScore(
            Driver driver,
            double startLat, double startLon,
            VehicleType vehicleType,
            boolean needsBabyFriendly,
            boolean needsPetFriendly,
            LocalDateTime scheduledTime
    ) {
        double score = 0;

        // Proveri da li je aktivan
        if (driver.getIsActive() == null || !driver.getIsActive()) {
            return -1;
        }

        // Proveri da li je blokiran
        if (Boolean.TRUE.equals(driver.getIsBlocked())) {
            return -1;
        }

        // Proveri da li ima vozilo
        if (driver.getVehicle() == null) {
            return -1;
        }

        // Proveri tip vozila
        if (!driver.getVehicle().getType().equals(vehicleType)) {
            return -1;
        }

        // Proveri vozilo
        if (needsBabyFriendly && !driver.getVehicle().getBabyFriendly()) {
            return -1;
        }
        if (needsPetFriendly && !driver.getVehicle().getPetFriendly()) {
            return -1;
        }

        // Proveri radne sate (max 8h u 24h)
        if (driver.getActiveHours() != null && driver.getActiveHours() > 8 * 3600) {
            return -1;
        }

        // Osnovni bodovi
        score += 100;

        // Bodovi za blizinu
        Double lat = driver.getVehicle().getCurrentLatitude();
        Double lon = driver.getVehicle().getCurrentLongitude();

        if (lat == null || lon == null) {
            lat = 0.0;
            lon = 0.0;
        }

        double distance = calculateDistance(startLat, startLon, lat, lon);
        double proximityScore = Math.max(0, 50 - (distance * 5));
        score += proximityScore;

        // Proveri trenutne i zakazane voznje
        List<RideStatus> activeStatuses = List.of(RideStatus.IN_PROGRESS, RideStatus.REQUESTED);
        List<Ride> activeRides = rideRepository.findByDriverIdAndRideStatusIn(
                driver.getId(), activeStatuses
        );

        if (activeRides.isEmpty()) {
            // Vozac je potpuno slobodan
            score += 30;
        } else {
            // Proveri konflikte
            boolean willBeFree = true;

            for (Ride ride : activeRides) {
                // Proveri voznje u toku
                if (ride.getRideStatus() == RideStatus.IN_PROGRESS) {
                    LocalDateTime estimatedEnd = ride.getStartTime()
                            .plusSeconds(ride.getRoute().getEstimatedTime());

                    long minutesUntilFree = Duration.between(
                            LocalDateTime.now(), estimatedEnd
                    ).toMinutes();

                    if (minutesUntilFree > 10) {
                        willBeFree = false;
                        break;
                    } else {
                        score += 10;
                    }
                }

                // Proveri zakazane voznje
                if (ride.getRideStatus() == RideStatus.REQUESTED && ride.getScheduledTime() != null) {
                    LocalDateTime existingStart = ride.getScheduledTime();
                    LocalDateTime existingEnd = existingStart.plusSeconds(
                            ride.getRoute().getEstimatedTime()
                    );

                    LocalDateTime newStart = scheduledTime;
                    LocalDateTime newEnd = scheduledTime.plusMinutes(30);

                    // Proveri preklapanje vremena
                    boolean overlaps = !(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd));

                    if (overlaps) {
                        willBeFree = false;
                        break;
                    }
                }
            }

            if (!willBeFree) {
                return -1;
            }
        }

        // Bonus za rejting
        double rating = driver.getAverageRating();
        score += rating * 5;

        return score;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in km
    }
}
