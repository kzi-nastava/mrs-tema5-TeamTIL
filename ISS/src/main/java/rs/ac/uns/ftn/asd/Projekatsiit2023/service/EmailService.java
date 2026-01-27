package rs.ac.uns.ftn.asd.Projekatsiit2023.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordEmail(String toEmail, String token) {

        String resetLink = "http://localhost:4200/reset-password?token=" + token;

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
}