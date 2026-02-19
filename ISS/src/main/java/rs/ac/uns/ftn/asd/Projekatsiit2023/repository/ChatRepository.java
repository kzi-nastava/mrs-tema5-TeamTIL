package rs.ac.uns.ftn.asd.Projekatsiit2023.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Chat;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
    Optional<Chat> findByUser_Email(String email);

    boolean existsByUser_Email(String email);

    @Query("SELECT c FROM Chat c JOIN FETCH c.user " +
            "ORDER BY (SELECT MAX(m.timestamp) FROM Message m WHERE m.chat = c) DESC NULLS LAST")
    List<Chat> findAllWithUsers();
}
