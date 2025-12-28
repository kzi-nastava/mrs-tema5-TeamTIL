package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.DriverRegistrationRequestDTO;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    // 2.2.3 Driver registration (admin f)
    @PostMapping
    public ResponseEntity<String> registerDriver(@RequestBody DriverRegistrationRequestDTO request) {
        // send an email with an activation link here
        return ResponseEntity.ok("Driver registration successful. Activation email sent to: " + request.getEmail());
    }
}
