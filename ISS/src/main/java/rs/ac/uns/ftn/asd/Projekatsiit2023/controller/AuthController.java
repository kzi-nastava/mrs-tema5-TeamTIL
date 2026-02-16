package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.LoginRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.RegisterRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.LoginResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.RegisterResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Account;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.AccountService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.DriverService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.utils.TokenUtils;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final TokenUtils tokenUtils;
    private final AccountService accountService;
    private final DriverService driverService;

    public AuthController(AuthenticationManager authenticationManager,
                          TokenUtils tokenUtils,
                          AccountService accountService,
                          DriverService driverService) {
        this.authenticationManager = authenticationManager;
        this.tokenUtils = tokenUtils;
        this.accountService = accountService;
        this.driverService = driverService;
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Account account = (Account) userDetails;

            String role = userDetails.getAuthorities().iterator().next()
                    .getAuthority().replace("ROLE_", "");

            String token = tokenUtils.generateToken(
                    userDetails.getUsername(),
                    role
            );

            // Ako je vozač, automatski ga prijavi na sistem
            if (account.getUserType() == UserType.DRIVER) {
                try {
                    logger.info("Attempting to login driver: {}", request.getEmail());
                    driverService.loginDriver(request.getEmail());
                    logger.info("Driver logged in successfully: {}", request.getEmail());
                } catch (Exception e) {
                    // Vozač je možda već prijavljen, ili došlo do greške
                    logger.warn("Driver login failed: {}", e.getMessage());
                    // Nastavi sa token-om čak i ako je login vozača fail
                }
            }

            LoginResponseDTO response = new LoginResponseDTO(
                    token,
                    role,
                    userDetails.getUsername(),
                    "Login successful",
                    account.getProfilePictureUrl()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoginResponseDTO response = new LoginResponseDTO(
                    null,
                    null,
                    null,
                    "Invalid email or password",
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // ================= REGISTRATION =================
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request
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

    // ================= TOKEN VALIDATION =================
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TokenValidationResponse(false, "No token provided"));
            }

            String token = authHeader.substring(7);
            String email = tokenUtils.getEmailFromToken(token);

            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TokenValidationResponse(false, "Invalid token"));
            }

            Account account = accountService.findByEmail(email);
            boolean isValid = tokenUtils.validateToken(token, account);

            if (isValid) {
                return ResponseEntity.ok(new TokenValidationResponse(true, "Token is valid"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TokenValidationResponse(false, "Token expired"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenValidationResponse(false, "Token validation failed"));
        }
    }

    // ================= LOGOUT =================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("=== LOGOUT REQUEST === Authorization Header: {}", authHeader != null ? "Present" : "Missing");

        try {
            if (authHeader == null || authHeader.isEmpty()) {
                logger.warn("No authorization header provided in logout request");
                return ResponseEntity.badRequest()
                        .body(new TokenValidationResponse(false, "No authorization header provided"));
            }

            if (!authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid authorization header format: {}", authHeader);
                return ResponseEntity.badRequest()
                        .body(new TokenValidationResponse(false, "Invalid authorization header format"));
            }

            String token = authHeader.substring(7);
            String email = tokenUtils.getEmailFromToken(token);
            logger.info("Extracted email from token: {}", email);

            if (email == null) {
                logger.warn("Could not extract email from token");
                return ResponseEntity.badRequest()
                        .body(new TokenValidationResponse(false, "Invalid token"));
            }

            Account account = accountService.findByEmail(email);
            logger.info("Found account: {}, UserType: {}", email, account != null ? account.getUserType() : "null");

            // Ako je vozač, automatski ga odjavi iz sistema
            if (account != null && account.getUserType() == UserType.DRIVER) {
                logger.info("Attempting to logout driver: {}", email);
                try {
                    driverService.logoutDriver(email);
                    logger.info("Driver logged out successfully: {}", email);
                } catch (Exception e) {
                    logger.error("Error logging out driver: {}", e.getMessage(), e);
                    // Vozač se ne može odjaviti (ima aktivnu vožnju ili nije prijavljen)
                    return ResponseEntity.badRequest()
                            .body(new TokenValidationResponse(false, e.getMessage()));
                }
            } else {
                logger.info("Account is not a driver or account is null");
            }

            logger.info("=== LOGOUT SUCCESS ===");
            return ResponseEntity.ok(new TokenValidationResponse(true, "Logout successful"));
        } catch (Exception e) {
            logger.error("Unexpected error during logout: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new TokenValidationResponse(false, "Logout failed: " + e.getMessage()));
        }
    }

    // Inner class for token validation response
    private static class TokenValidationResponse {
        public boolean valid;
        public String message;

        public TokenValidationResponse(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
    }
}
