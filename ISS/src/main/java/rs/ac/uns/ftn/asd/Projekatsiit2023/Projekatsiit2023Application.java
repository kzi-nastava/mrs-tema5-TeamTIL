package rs.ac.uns.ftn.asd.Projekatsiit2023;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Projekatsiit2023Application {

    public static void main(String[] args) {
        SpringApplication.run(Projekatsiit2023Application.class, args);
    }
}