package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.InconsistencyReport;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.RegisteredUser;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.InconsistencyReportRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RegisteredUserRepository;
import rs.ac.uns.ftn.asd.Projekatsiit2023.repository.RideRepository;

import java.time.LocalTime;

@Service
public class InconsistencyReportService {

    @Autowired
    private InconsistencyReportRepository reportRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Transactional
    public void saveReportWithAttachment(
            Integer rideId,
            String passengerEmail,
            String description,
            String base64File // optional
    ) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Ride not found"));

        RegisteredUser reporter = registeredUserRepository.findByEmail(passengerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Reporter not found"));

        InconsistencyReport report = new InconsistencyReport();
        report.setRide(ride);
        report.setReportedBy(reporter);
        report.setDescription(description);
        report.setTimeReported(LocalTime.now());

        if (base64File != null && !base64File.isEmpty()) {
            String publicId = "ride_" + rideId + "_report_" + System.currentTimeMillis();
            String fileUrl = cloudinaryService.uploadBase64Image(base64File, publicId);
            report.setAttachmentUrl(fileUrl);
        }

        reportRepository.save(report);
    }
}
