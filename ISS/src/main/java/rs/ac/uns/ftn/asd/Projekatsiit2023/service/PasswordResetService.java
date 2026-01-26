package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.PasswordResetToken;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.RegisteredUser;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PasswordResetTokenRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RegisteredUserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final RegisteredUserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(RegisteredUserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                EmailService emailService,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void requestPasswordReset(String email) {

        userRepository.findByEmail(email).ifPresent(user -> {

            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = new PasswordResetToken(
                    token,
                    user,
                    LocalDateTime.now().plusMinutes(30)
            );

            tokenRepository.save(resetToken);

            emailService.sendResetPasswordEmail(user.getEmail(), token);
        });
    }

    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.isExpired() || resetToken.isUsed()) {
            throw new RuntimeException("Token expired or already used");
        }

        RegisteredUser user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}