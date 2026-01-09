package rs.ac.uns.ftn.asd.Projekatsiit2023;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("rs.ac.uns.ftn.asd.Projekatsiit2023.model")
@EnableJpaRepositories("rs.ac.uns.ftn.asd.Projekatsiit2023.repository")
public class Projekatsiit2023Application {
    public static void main(String[] args) {
        SpringApplication.run(Projekatsiit2023Application.class, args);
    }
}