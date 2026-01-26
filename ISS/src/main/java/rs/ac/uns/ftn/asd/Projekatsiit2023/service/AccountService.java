package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.asd.Projekatsiit2023.dto.request.RegisterRequestDTO;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Account;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.RegisteredUser;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.AccountRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RegisteredUserRepository;

@Service
public class AccountService implements UserDetailsService {

    private final CloudinaryService cloudinaryService;
    private final AccountRepository accountRepository;
    private final RegisteredUserRepository registeredUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AccountService(CloudinaryService cloudinaryService,
                          AccountRepository accountRepository,
                          RegisteredUserRepository registeredUserRepository,
                          @Lazy BCryptPasswordEncoder passwordEncoder) {
        this.cloudinaryService = cloudinaryService;
        this.accountRepository = accountRepository;
        this.registeredUserRepository = registeredUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Account findById(Integer id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public Account register(RegisterRequestDTO dto) {
        if (accountRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        RegisteredUser registeredUser = new RegisteredUser();
        registeredUser.setEmail(dto.getEmail());
        registeredUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        registeredUser.setFirstName(dto.getName());
        registeredUser.setLastName(dto.getSurname());
        registeredUser.setPhoneNumber(dto.getPhoneNumber());
        registeredUser.setAddress(dto.getCity());
        registeredUser.setUserType(UserType.REGISTERED_USER);
        registeredUser.setIsBlocked(false);

        if (dto.getProfilePictureUrl() != null && !dto.getProfilePictureUrl().isEmpty()) {
            String publicId = "user_" + dto.getEmail().replace("@", "_").replace(".", "_");
            String imageUrl = cloudinaryService.uploadBase64Image(dto.getProfilePictureUrl(), publicId);
            registeredUser.setProfilePictureUrl(imageUrl);
        }

        return registeredUserRepository.save(registeredUser);
    }

    @Transactional
    public void updateProfilePicture(Integer accountId, String base64Image) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        String oldImageUrl = account.getProfilePictureUrl();
        if (oldImageUrl != null && oldImageUrl.contains("cloudinary")) {
            String publicId = extractPublicIdFromUrl(oldImageUrl);
            cloudinaryService.deleteImage(publicId);
        }

        String publicId = "user_" + account.getEmail().replace("@", "_").replace(".", "_");
        String newImageUrl = cloudinaryService.uploadBase64Image(base64Image, publicId);

        account.setProfilePictureUrl(newImageUrl);
        accountRepository.save(account);
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        String[] parts = imageUrl.split("/");
        String fileName = parts[parts.length - 1];
        return "tiltaxi/profiles/" + fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public Account findByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // provera da li se stara lozinka poklapa sa onom u bazi
        if (!passwordEncoder.matches(oldPassword, account.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // hashovanje nove lozinke i cuvanje
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}
