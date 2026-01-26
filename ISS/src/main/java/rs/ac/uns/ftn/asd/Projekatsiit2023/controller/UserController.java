package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.UserResponseDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Account;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.AccountService;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final AccountService accountService;

    public UserController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/my-profile")
    @PreAuthorize("hasAnyRole('REGISTERED_USER', 'ADMINISTRATOR', 'DRIVER')")
    public ResponseEntity<UserResponseDTO> getMyProfile() {
        // Dobijamo email iz tokena ulogovanog korisnika
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Account account = accountService.findByEmail(email);

        // Pakujemo podatke iz baze u DTO
        UserResponseDTO userDTO = new UserResponseDTO(
                account.getId().intValue(),
                account.getFirstName(),
                account.getLastName(),
                account.getEmail(),
                account.getPhoneNumber(),
                account.getAddress(),
                account.getProfilePictureUrl()
        );

        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/my-profile")
    @PreAuthorize("hasAnyRole('REGISTERED_USER', 'ADMINISTRATOR', 'DRIVER')")
    public ResponseEntity<UserResponseDTO> updateMyProfile(@RequestBody UserResponseDTO updatedData) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Account account = accountService.findByEmail(email);

        String newImageData = updatedData.getProfilePictureUrl();
        if (newImageData != null && !newImageData.startsWith("http")) {
            accountService.updateProfilePicture(account.getId().intValue(), newImageData);
            account = accountService.findByEmail(email);
        }

        account.setFirstName(updatedData.getFirstName());
        account.setLastName(updatedData.getLastName());
        account.setAddress(updatedData.getAddress());
        account.setPhoneNumber(updatedData.getPhoneNumber());

        accountService.save(account);

        updatedData.setProfilePictureUrl(account.getProfilePictureUrl());
        return ResponseEntity.ok(updatedData);
    }

    @PutMapping("/change-password")
    @PreAuthorize("hasAnyRole('REGISTERED_USER', 'ADMINISTRATOR', 'DRIVER')")
    public ResponseEntity<String> changePassword(@RequestBody rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.ChangePasswordRequestDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // uzimamo email iz tokena ulogovanog korisnika

        try {
            accountService.changePassword(email, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password successfully changed");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
