package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.PanicNotification;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.PanicNotificationRepository;

@Service
public class NotificationService {

    @Autowired
    private PanicNotificationRepository panicNotificationRepository;

    @Transactional
    public void savePanicNotification(PanicNotification panicNotification) {
        panicNotificationRepository.save(panicNotification);
    }
}