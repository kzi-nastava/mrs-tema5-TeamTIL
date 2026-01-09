package rs.ac.uns.ftn.asd.Projekatsiit2023.model;

import jakarta.persistence.*;
import lombok.*;
import rs.ac.uns.ftn.asd.Projekatsiit2023.enumeration.UserType;

import java.time.LocalTime;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "content")
    private String content;

    @Column(name = "timestamp")
    private LocalTime timestamp;

    @Column(name = "user_type")
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;
}
