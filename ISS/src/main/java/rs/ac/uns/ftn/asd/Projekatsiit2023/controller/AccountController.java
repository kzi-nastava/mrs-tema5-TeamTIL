package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Account;
import rs.ac.uns.ftn.asd.Projekatsiit2023.service.AccountService;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PutMapping("/{id}/profile-picture")
    public ResponseEntity<Void> updateProfilePicture(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {

        accountService.updateProfilePicture(id, request.get("base64Image"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/profile-picture")
    public ResponseEntity<String> getProfilePicture(@PathVariable Integer id) {
        Account account = accountService.findById(id);

        if (account.getProfilePictureUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(account.getProfilePictureUrl());
    }
}
