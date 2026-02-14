package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.AssignedRideDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.RideHistoryDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.DriverRideDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.RideRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.RideStatus;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PanicNotificationRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PriceConfigRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RegisteredUserRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RideRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Locale;

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

    private final RideRepository rideRepository;

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
                    panicSent
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
    public RideHistoryDTO createNewRide(RideRequestDTO request) {
        Ride ride = new Ride();

        ride.setRideStatus(RideStatus.REQUESTED); // Ili ACCEPTED zavisno od tvoje logike

        return new RideHistoryDTO(
                101,
                request.getPassengerEmails().isEmpty() ? "guest@example.com" : request.getPassengerEmails().get(0),
                "pending@driver.com",
                request.getLocations().get(0),
                request.getLocations().get(request.getLocations().size() - 1),
                "REQUESTED",
                1200.0,
                java.time.LocalDateTime.now().toString()
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

        // Route estimation for distance and duration
        RouteService.RouteEstimation estimation = routeService.estimateRoute(
                ride.getStartLocation().getLatitude(),
                ride.getStartLocation().getLongitude(),
                ride.getEndLocation().getLatitude(),
                ride.getEndLocation().getLongitude()
        );
        double distance = estimation != null ? estimation.distanceKm() : 0.0;
        double duration = estimation != null ? estimation.durationMin() : 0.0;

        // Use actual end_time for FINISHED rides, otherwise calculate estimated end time
        String endTimeFormatted = null;
        if (ride.getRideStatus() == RideStatus.FINISHED && ride.getEndTime() != null) {
            endTimeFormatted = ride.getEndTime().format(formatter);
        } else if (ride.getStartTime() != null && duration > 0) {
            endTimeFormatted = ride.getStartTime().plusMinutes((long) duration).format(formatter);
        }

        // Use totalPrice from DB for FINISHED rides, otherwise calculate
        double price;
        if (ride.getRideStatus() == RideStatus.FINISHED && ride.getTotalPrice() != null) {
            price = ride.getTotalPrice();
        } else {
            price = calculateFinalPrice(
                    ride.getDriver() != null ? ride.getDriver().getVehicle().getType() : VehicleType.STANDARD,
                    ride.getStartLocation(),
                    ride.getEndLocation()
            );
        }

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
                                .map(co -> new rs.ac.uns.ftn.asd.Projekatsiit2023.dto.LinkdPassengerDTO(
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
            durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(ride.getStartTime(), ride.getEndTime());
        }
        return new rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RideStopResponseDTO(
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
                    panicSent
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
                    panicSent
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
}
