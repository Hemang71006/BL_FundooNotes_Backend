package com.fundoonotes.messaging;

import com.fundoonotes.entity.User;
import com.fundoonotes.repository.UserRepository;

import jakarta.jms.JMSException;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class UserRegistrationJmsConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(UserRegistrationJmsConsumer.class);

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final String fromEmail;

    public UserRegistrationJmsConsumer(
            UserRepository userRepository,
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromEmail) {

        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @JmsListener(destination = "${app.jms.queue.user-registration}")
    public void receiveRegistrationMessage(Map<String, Object> message) {

        Object userIdValue = message.get("userId");

        if (userIdValue == null) {
            throw new IllegalArgumentException(
                    "Registration message does not contain userId"
            );
        }

        Long userId = Long.valueOf(userIdValue.toString());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Registered user not found: " + userId
                ));

        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setFrom(fromEmail);
        mail.setTo(user.getEmail());
        mail.setSubject("Welcome to Fundoo Notes!");

        mail.setText(
                "Hello " + user.getFirstName() + ",\n\n" +
                        "Welcome to Fundoo Notes!\n\n" +
                        "Your account has been successfully registered.\n" +
                        "You can now log in and start organizing your notes.\n\n" +
                        "Regards,\nFundoo Notes Team"
        );

        mailSender.send(mail);

        logger.info(
                "Registration welcome email sent for userId={}",
                userId
        );
    }
}