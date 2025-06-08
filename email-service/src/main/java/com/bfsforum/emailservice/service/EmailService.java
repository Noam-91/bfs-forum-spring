package com.bfsforum.emailservice.service;

import com.bfsforum.emailservice.dao.VerificationTokenRepository;
import com.bfsforum.emailservice.dto.VerificationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailService {

    @Value("${app.activation.base-url}")
    private String activationBaseUrl;

    @Value("${token.expiration.minutes}")
    private long expirationMinutes;

    @Value("${app.email.from}")
    private String fromEmail;

    private final VerificationTokenRepository tokenRepository;

    public EmailService(VerificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void sendActivationEmail(String toEmail, UUID userId) throws Exception {
        Optional<VerificationToken> existingToken = tokenRepository.findByUserId(userId);

        if (existingToken.isPresent()) {
            VerificationToken token = existingToken.get();
            if (Duration.between(token.getCreatedAt(), Instant.now()).toMinutes() < expirationMinutes) {
                throw new IllegalStateException("Activation link already sent. " +
                        "Please wait up to " + expirationMinutes + " minutes.");
            }
        }

        String tokenStr = UUID.randomUUID().toString();

        VerificationToken newToken = new VerificationToken();
        newToken.setToken(tokenStr);
        newToken.setUserId(userId);

        tokenRepository.save(newToken);

        String activationLink = activationBaseUrl + "/activate?token=" + tokenStr;
        String subject = "Activate your account";
        String body = "Click the link to activate (valid for " + expirationMinutes + " minutes):\n" + activationLink;

        var email = CreateEmail.createEmail(toEmail, fromEmail, subject, body);
        SendMessage.sendEmail(email);
    }


    public Optional<VerificationToken> resolveToken(String token) {
        return tokenRepository.findById(token)
                .filter(t -> t.getExpiredAt().isAfter(Instant.now()));
    }



    public void consumeToken(String token) {
        tokenRepository.findById(token)
                .filter(t -> t.getExpiredAt().isAfter(Instant.now()))
                .map(t -> {
                    tokenRepository.delete(t);
                    return t.getUserId().toString(); // or return email if you store that too
                });
    }
}
