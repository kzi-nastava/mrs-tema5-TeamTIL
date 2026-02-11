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
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.ActivationRequestDTO;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    /*@PostMapping("/activate")
    public ResponseEntity<String> activateDriver(@RequestBody ActivationRequestDTO request) {
        driverService.activateDriverAccount(request);
        return ResponseEntity.ok("Account successfully activated! You can now log in.");
    }*/

    @PostMapping("/activate")
    public ResponseEntity<Map<String, String>> activateDriver(@RequestBody ActivationRequestDTO request) {
        driverService.activateDriverAccount(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Account successfully activated! You can now log in.");
        return ResponseEntity.ok(response);
    }

    // 2.2.3 Driver registration (admin f)
    /*@PostMapping
    public ResponseEntity<String> registerDriver(@RequestBody DriverRegistrationRequestDTO request) {
        // pozivamo servis da stvarno odradi logiku cuvanja vozaca i vozila
        driverService.registerDriver(request);

        return ResponseEntity.ok("Driver registration successful. Activation email sent to: " + request.getEmail());
    }*/

    @PostMapping
    public ResponseEntity<Map<String, String>> registerDriver(@RequestBody DriverRegistrationRequestDTO request) {
        driverService.registerDriver(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Driver registration successful. Activation email sent to: " + request.getEmail());
        return ResponseEntity.ok(response);
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
