package rs.ac.uns.ftn.asd.Projekatsiit2023.config;

import jakarta.transaction.Transactional;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Account;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Driver;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.RegisteredUser;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.AccountRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.DriverRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RegisteredUserRepository;

@Configuration
public class DataLoader {

    @Bean
    @Transactional
    public ApplicationRunner initializer(AccountRepository accountRepository,
                                         RegisteredUserRepository registeredUserRepository,
                                         DriverRepository driverRepository,
                                         PasswordEncoder passwordEncoder) {
        return args -> {
            if (accountRepository.count() == 0) {
                // Admin
                Account admin = new Account();
                admin.setEmail("admin@tiltaxi.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setPhoneNumber("0601234567");
                admin.setUserType(UserType.ADMINISTRATOR);
                admin.setIsBlocked(false);
                accountRepository.save(admin);

                // Registered User
                RegisteredUser registeredUser = new RegisteredUser();
                registeredUser.setEmail("user@tiltaxi.com");
                registeredUser.setPassword(passwordEncoder.encode("user123"));
                registeredUser.setFirstName("Test");
                registeredUser.setLastName("Korisnik");
                registeredUser.setPhoneNumber("0601234568");
                registeredUser.setUserType(UserType.REGISTERED_USER);
                registeredUser.setIsBlocked(false);
                registeredUserRepository.save(registeredUser);

                // Driver
                Driver driver = new Driver();
                driver.setEmail("driver@tiltaxi.com");
                driver.setPassword(passwordEncoder.encode("driver123"));
                driver.setFirstName("Vozac");
                driver.setLastName("Taksista");
                driver.setPhoneNumber("0601234569");
                driver.setUserType(UserType.DRIVER);
                driver.setIsBlocked(false);
                driver.setIsActive(false);
                driver.setActiveHours(0L);
                driverRepository.save(driver);

                System.out.println("✅ Test korisnici kreirani!");
            }
        };
    }
}
