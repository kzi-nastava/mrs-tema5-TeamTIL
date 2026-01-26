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
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.DriverResponseDTO;

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

    @Transactional
    public DriverResponseDTO getDriverProfile(String email) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        Vehicle v = driver.getVehicle();

        return new DriverResponseDTO(
                driver.getId().intValue(),
                driver.getFirstName(),
                driver.getLastName(),
                driver.getEmail(),
                driver.getPhoneNumber(),
                driver.getAddress(),
                driver.getProfilePictureUrl(),
                v != null ? v.getModel() : "",
                (v != null && v.getType() != null) ? v.getType().name() : "", // Enum u String
                v != null ? v.getLicensePlate() : "",
                v != null ? v.getCapacity() : 0,
                v != null ? v.getBabyFriendly() : false,
                v != null ? v.getPetFriendly() : false,
                driver.getIsActive()
        );
    }

    @Transactional
    public DriverResponseDTO updateDriverProfile(String email, DriverResponseDTO dto) {
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (dto.getProfilePictureUrl() != null && !dto.getProfilePictureUrl().startsWith("http")) {
            String publicId = "driver_" + email.replace("@", "_").replace(".", "_");
            String imageUrl = cloudinaryService.uploadBase64Image(dto.getProfilePictureUrl(), publicId);
            driver.setProfilePictureUrl(imageUrl);
            dto.setProfilePictureUrl(imageUrl);
        }

        driver.setFirstName(dto.getFirstName());
        driver.setLastName(dto.getLastName());
        driver.setAddress(dto.getAddress());
        driver.setPhoneNumber(dto.getPhoneNumber());
        driver.setIsActive(dto.getIsActive());

        Vehicle v = driver.getVehicle();
        if (v != null) {
            v.setModel(dto.getVehicleModel());
            v.setLicensePlate(dto.getLicensePlate());
            v.setCapacity(dto.getPassengerCapacity());
            v.setBabyFriendly(dto.getBabyFriendly());
            v.setPetFriendly(dto.getPetFriendly());

            if (dto.getVehicleType() != null) {
                v.setType(rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.VehicleType.valueOf(dto.getVehicleType().toUpperCase()));
            }
        }

        driverRepository.save(driver);
        return dto;
    }
}