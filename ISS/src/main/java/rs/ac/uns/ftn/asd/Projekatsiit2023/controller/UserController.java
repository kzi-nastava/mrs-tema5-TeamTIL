package rs.ac.uns.ftn.asd.Projekatsiit2023.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.response.UserResponseDTO;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // 2.3 View profile
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserProfile(@PathVariable Integer id) {
        UserResponseDTO user = new UserResponseDTO(
                id, "Marko", "Marković", "marko@example.com",
                "064123456", "Bulevar Oslobođenja 1", "url_to_img"
        );
        return ResponseEntity.ok(user);
    }

    // 2.3 Profile update
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUserProfile(
            @PathVariable Integer id,
            @RequestBody UserResponseDTO updatedUser) {
        return ResponseEntity.ok(updatedUser);
    }
}
