package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.DriverRegistrationRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.DriverService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.DriverResponseDTO;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    // 2.2.3 Driver registration (admin f)
    @PostMapping
    public ResponseEntity<String> registerDriver(@RequestBody DriverRegistrationRequestDTO request) {
        // pozivamo servis da stvarno odradi logiku cuvanja vozaca i vozila
        driverService.registerDriver(request);

        return ResponseEntity.ok("Driver registration successful. Activation email sent to: " + request.getEmail());
    }

    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverResponseDTO> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        DriverResponseDTO driverDTO = driverService.getDriverProfile(email);
        return ResponseEntity.ok(driverDTO);
    }

    @PutMapping("/my-profile")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverResponseDTO> updateMyProfile(@RequestBody DriverResponseDTO updatedData) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        DriverResponseDTO response = driverService.updateDriverProfile(email, updatedData);
        return ResponseEntity.ok(response);
    }
}
