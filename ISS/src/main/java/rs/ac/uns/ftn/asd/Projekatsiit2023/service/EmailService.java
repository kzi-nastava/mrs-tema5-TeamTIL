package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.Projekatsiit2023.model.Ride;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private static final String BASE_URL = "http://localhost:4200";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordEmail(String toEmail, String token) {
        String resetLink = "http://localhost/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset");
        message.setText(
                "Hello,\n\n" +
                        "You requested a password reset.\n" +
                        "Please click the link below to set a new password:\n\n" +
                        resetLink +
                        "\n\nThis link is valid for 30 minutes.\n\n" +
                        "If you did not request this, you can safely ignore this email."
        );

        mailSender.send(message);
    }

    public void sendActivationEmail(String toEmail, String token) {
        String activationLink = "http://localhost:4200/new-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Activate your Driver Account");
        message.setText(
                "Hello,\n\n" +
                        "An administrator has created a driver account for you.\n" +
                        "Please click the link below to activate your account and set your password:\n\n" +
                        activationLink +
                        "\n\nThis link is valid for 24 hours.\n\n" +
                        "Welcome to the team!"
        );

        mailSender.send(message);
    }

    public void sendRideFinishedEmail(String toEmail, Ride ride){
        String rateLink = BASE_URL + "/user-ride-history";
        String newRideLink = BASE_URL + "/book";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Ride Has Been Completed – Thank You for Riding With Us");

        String text = "Dear Customer,\n\n" +
                "Your ride has been successfully completed.\n\n" +
                "--------------- Ride Details ---------------\n" +
                "From: " + ride.getStartLocation().getAddress() + "\n" +
                "To: " + ride.getEndLocation().getAddress() + "\n" +
                "Driver: " + ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName() + "\n" +
                "Price: " + String.format("%.2f", ride.getTotalPrice()) + " RSD\n" +
                "-------------------------------------------\n\n" +
                "Please rate your experience by clicking the following link:\n" +
                rateLink + "\n\n" +
                "Would you like to book another ride? Visit:\n" +
                newRideLink + "\n\n" +
                "Kind regards,\nYour Support Team.";

        message.setText(text);

        mailSender.send(message);
    }

    public void sendRideFinishedEmailToCoPassenger(String toEmail, Ride ride) {
        String newRideLink = BASE_URL + "/book";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Ride Has Been Completed – Thank You for Riding With Us");

        String text = "Dear Customer,\n\n" +
                "Your ride has been successfully completed.\n\n" +
                "--------------- Ride Details ---------------\n" +
                "From: " + ride.getStartLocation().getAddress() + "\n" +
                "To: " + ride.getEndLocation().getAddress() + "\n" +
                "Driver: " + ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName() + "\n" +
                "Price: " + String.format("%.2f", ride.getTotalPrice()) + " RSD\n" +
                "-------------------------------------------\n\n" +
                "Would you like to book your own ride? Visit:\n" +
                newRideLink + "\n\n" +
                "Kind regards,\nYour Support Team.";

        message.setText(text);
        mailSender.send(message);
    }

    public void sendRideAcceptedEmail(String toEmail, Ride ride) {
        String trackLink = BASE_URL + "/track-ride/" + ride.getId();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("You've Been Added to a Ride – Track Your Journey");

        String text = "Dear Customer,\n\n" +
                "Great news! You have been added to a ride that has been successfully accepted.\n\n" +
                "--------------- Ride Details ---------------\n" +
                "From: " + ride.getStartLocation().getAddress() + "\n" +
                "To: " + ride.getEndLocation().getAddress() + "\n" +
                "Driver: " + ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName() + "\n" +
                "Vehicle: " + ride.getDriver().getVehicle().getModel() + "\n" +
                "Scheduled: " + ride.getScheduledTime() + "\n" +
                "-------------------------------------------\n\n" +
                "You can track your ride in real time by clicking the link below:\n" +
                trackLink + "\n\n" +
                "Kind regards,\nYour Support Team.";

        message.setText(text);
        mailSender.send(message);
    }
}