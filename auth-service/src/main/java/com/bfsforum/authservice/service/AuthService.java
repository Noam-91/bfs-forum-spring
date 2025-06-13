package com.bfsforum.authservice.service;

import com.bfsforum.authservice.domain.User;
import com.bfsforum.authservice.dto.LoginRequest;
import com.bfsforum.authservice.exception.InvalidCredentialsException;
import com.bfsforum.authservice.repository.AuthDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class AuthService {
  private final AuthDao authDao;
  private final JwtEncoder jwtEncoder;
  private final PasswordEncoder passwordEncoder;

  public AuthService(AuthDao authDao, JwtEncoder jwtEncoder, PasswordEncoder passwordEncoder) {
    this.authDao = authDao;
    this.jwtEncoder = jwtEncoder;
    this.passwordEncoder = passwordEncoder;
  }
  private static final long TOKEN_EXPIRE = 36000L;        // 10 hours

  /** Validate user and issue token
   * @param loginRequest
   * @return JWT token
   * @throws InvalidCredentialsException
   */
  public String loginAndIssueToken(LoginRequest loginRequest) throws InvalidCredentialsException{
    String username = loginRequest.getUsername();
    String password = loginRequest.getPassword();

    // validate username, password
    Optional<User> userOptional = authDao.findByUsername(username);
    if(userOptional.isEmpty()){
      log.info("User does not exist");
      throw new InvalidCredentialsException("Incorrect credentials, please try again.");
    }
    User user = userOptional.get();
    if(!passwordEncoder.matches(password, user.getPassword())){
      log.info("Password does not match");
      throw new InvalidCredentialsException("Incorrect credentials, please try again.");
    }

    log.info("User {} logged in successfully", user.getUsername());

    // issue token
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("auth-service")
        .issuedAt(now)
        .expiresAt(now.plusSeconds(TOKEN_EXPIRE))
        .subject(user.getUsername())
        .claim("userId", user.getId())
        .claim("role", user.getRole())
        .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  /** Find user by id
   * @param userId
   * @return User
   */
  public User findUserById(String userId){
    return authDao.findById(userId).orElseThrow(()->new RuntimeException("User not found"));
  }
}
