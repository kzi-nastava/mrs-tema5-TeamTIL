package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.LoginRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.RegisterRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.LoginResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RegisterResponseDTO;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = new LoginResponseDTO(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", // mock JWT token
                "DRIVER", // or "REGISTERED_USER", "ADMINISTRATOR"
                request.getEmail(),
                "Login successful"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        RegisterResponseDTO response = new RegisterResponseDTO(
                request.getEmail(),
                request.getUserType() != null ? request.getUserType() : "REGISTERED_USER",
                "Registration successful"
        );

        return ResponseEntity.ok(response);
    }
}
