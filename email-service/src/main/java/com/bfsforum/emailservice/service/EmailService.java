package com.bfsforum.emailservice.service;

import com.bfsforum.emailservice.dao.UserRepository;
import com.bfsforum.emailservice.dto.Role;
import com.bfsforum.emailservice.dto.TokenInfo;
import com.bfsforum.emailservice.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Value("${app.activation.base-url}")
    String activationBaseUrl;

    @Value("${token.expiration.minutes}")
    long expirationMinutes;

    @Value("${app.email.from}")
    String fromEmail;
    @Autowired
    private UserRepository userRepository;
    final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    public void sendActivationEmail(String toEmail) throws Exception {
        // Check for existing valid token
        for (Map.Entry<String, TokenInfo> entry : tokenStore.entrySet()) {
            TokenInfo info = entry.getValue();
            if (info.getEmail().equals(toEmail) &&
                    Duration.between(info.getCreatedAt(), Instant.now()).toMinutes() < expirationMinutes) {
                throw new IllegalStateException("Activation link already sent. Please wait up to 10 minutes.");
            }
        }

        String token = UUID.randomUUID().toString();
        tokenStore.put(token, new TokenInfo(toEmail, Instant.now()));

        String activationLink = activationBaseUrl + "/activate?token=" + token;
        String subject = "Activate your account";
        String body = "Click the link to activate (valid for " + expirationMinutes + " minutes):\n" + activationLink;

        var email = CreateEmail.createEmail(toEmail, fromEmail, subject, body);
        SendMessage.sendEmail(email);
    }


    public boolean validateToken(String token) {
        TokenInfo info = tokenStore.get(token);
        if (info == null) return false;
        return Duration.between(info.getCreatedAt(), Instant.now()).toMinutes() < expirationMinutes;
    }

    public String consumeToken(String token) {
        TokenInfo info = tokenStore.remove(token);
        if (info != null) {
            String email = info.getEmail();
            Optional<User> userOpt = userRepository.findByUsername(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setActive(true);
                user.setRole(Role.USER); // assume Role is an enum
                userRepository.save(user);
            }
            return email;
        }
        return null;
    }
}
