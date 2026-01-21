package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.LoginRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.LoginResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.RegisteredUser;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.DriverRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RegisteredUserRepository;

import java.util.Optional;

@Service
public class AuthService {

    private final RegisteredUserRepository registeredUserRepository;
    private final DriverRepository driverRepository;

    public AuthService(RegisteredUserRepository registeredUserRepository,
                       DriverRepository driverRepository) {
        this.registeredUserRepository = registeredUserRepository;
        this.driverRepository = driverRepository;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {

        // 1. pokušaj da nađeš korisnika po email-u
        Optional<RegisteredUser> userOpt =
                registeredUserRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        RegisteredUser user = userOpt.get();

        // 2. proveri lozinku (za sada plain-text, kasnije BCrypt)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        // 3. napravi response
        return new LoginResponseDTO(
                "REAL_JWT_LATER",
                user.getUserType().name(),
                user.getEmail(),
                "Login successful"
        );
    }
}
