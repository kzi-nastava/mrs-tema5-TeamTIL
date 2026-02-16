package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Account;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.AccountService;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.BlockUserRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.UserListItemDTO;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountService accountService;

    @PutMapping("/{id}/profile-picture")
    @PreAuthorize("hasAnyRole('REGISTERED_USER', 'ADMINISTRATOR', 'DRIVER')")
    public ResponseEntity<Void> updateProfilePicture(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {

        accountService.updateProfilePicture(id, request.get("base64Image"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/profile-picture")
    @PreAuthorize("hasAnyRole('REGISTERED_USER', 'ADMINISTRATOR', 'DRIVER')")
    public ResponseEntity<String> getProfilePicture(@PathVariable Integer id) {
        Account account = accountService.findById(id);

        if (account.getProfilePictureUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(account.getProfilePictureUrl());
    }

    @GetMapping("/drivers")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<UserListItemDTO>> getAllDrivers() {
        List<Account> drivers = accountService.getAllDrivers();

        List<UserListItemDTO> driverDTOs = drivers.stream()
                .map(driver -> new UserListItemDTO(
                        driver.getId(),
                        driver.getFirstName(),
                        driver.getLastName(),
                        driver.getEmail(),
                        driver.getPhoneNumber(),
                        "DRIVER",
                        driver.getIsBlocked(),
                        driver.getBlockReason(),
                        driver.getProfilePictureUrl()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(driverDTOs);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<UserListItemDTO>> getAllRegisteredUsers() {
        List<Account> users = accountService.getAllRegisteredUsers();

        List<UserListItemDTO> userDTOs = users.stream()
                .map(user -> new UserListItemDTO(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        "REGISTERED_USER",
                        user.getIsBlocked(),
                        user.getBlockReason(),
                        user.getProfilePictureUrl()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    @PostMapping("/block")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> blockUser(@RequestBody BlockUserRequestDTO request) {
        accountService.blockUser(request.getUserId(), request.getBlock(), request.getReason());
        return ResponseEntity.ok().build();
    }
}
