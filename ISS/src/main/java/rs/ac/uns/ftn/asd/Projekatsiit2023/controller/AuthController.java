package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.LoginRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.RegisterRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.LoginResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RegisterResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Account;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.AccountService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.utils.TokenUtils;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenUtils tokenUtils;
    private final AccountService accountService;

    public AuthController(AuthenticationManager authenticationManager,
                          TokenUtils tokenUtils,
                          AccountService accountService) {
        this.authenticationManager = authenticationManager;
        this.tokenUtils = tokenUtils;
        this.accountService = accountService;
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String role = userDetails.getAuthorities().iterator().next()
                    .getAuthority().replace("ROLE_", "");

            String token = tokenUtils.generateToken(
                    userDetails.getUsername(),
                    role
            );

            LoginResponseDTO response = new LoginResponseDTO(
                    token,
                    role,
                    userDetails.getUsername(),
                    "Login successful"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoginResponseDTO response = new LoginResponseDTO(
                    null,
                    null,
                    null,
                    "Invalid email or password"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // ================= REGISTRATION =================
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(
            @RequestBody RegisterRequestDTO request
    ) {
        try {
            Account account = accountService.register(request);

            RegisterResponseDTO response = new RegisterResponseDTO(
                    account.getEmail(),
                    account.getUserType().name(),
                    "Registration successful"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new RegisterResponseDTO(null, null, e.getMessage()));
        }
    }
}
