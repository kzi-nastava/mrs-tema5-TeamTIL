package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.DriverStatusEventType;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "driver_status_event")
public class DriverStatusEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatusEventType eventType;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public DriverStatusEvent(Driver driver, DriverStatusEventType eventType) {
        this.driver = driver;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }
}

