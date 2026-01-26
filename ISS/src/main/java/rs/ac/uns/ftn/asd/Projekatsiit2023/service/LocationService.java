package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Location;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Route;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.LocationRepository;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location findOrSaveLocation(Location location, Route route) {
        location.setRoute(route);

        return locationRepository
                .findByLatitudeAndLongitudeAndAddress(
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAddress()
                )
                .orElseGet(() -> saveLocation(location));
    }

    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }
}