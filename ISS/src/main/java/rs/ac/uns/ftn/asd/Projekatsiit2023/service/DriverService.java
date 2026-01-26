package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.DriverRegistrationRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Driver;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Vehicle;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.DriverRepository;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

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
        driver.setIsActive(false);

        // trenutno fiksna lozinka, dodacemo slanje mejla
        driver.setPassword(passwordEncoder.encode("sifra1234"));

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

        driver.setVehicle(vehicle);
        vehicle.setDriver(driver);

        driverRepository.save(driver);
    }
}