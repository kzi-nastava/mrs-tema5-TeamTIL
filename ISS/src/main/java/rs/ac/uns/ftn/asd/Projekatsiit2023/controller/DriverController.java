package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.DriverRegistrationRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.DriverService;

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
}
