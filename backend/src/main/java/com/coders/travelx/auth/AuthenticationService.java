package com.coders.travelx.auth;


import com.coders.travelx.config.JwtService;
import com.coders.travelx.model.Token;
import com.coders.travelx.model.User;
import com.coders.travelx.model.VerificationCode;
import com.coders.travelx.repository.TokenRepository;
import com.coders.travelx.repository.UserRepository;
import com.coders.travelx.repository.VerificationCodeRepository;
import com.coders.travelx.util.Role;
import com.coders.travelx.util.TokenType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationCodeRepository verificationCodeRepository;



    public User register(RegisterRequest request) {

        if (repository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException();
        }
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(false)
                .build();
        var savedUser = repository.save(user);

        return savedUser;
//        var jwtToken = jwtService.generateToken(user);
//        var refreshToken = jwtService.generateRefreshToken(user);
//        saveUserToken(savedUser, jwtToken);
//        return AuthenticationResponse.builder()
//                .accessToken(jwtToken)
//                .refreshToken(refreshToken)
//                .role(request.getRole())
//                .build();
    }


    public String validateVerificationCode(String code) {
        VerificationCode verificationCode = verificationCodeRepository.findByCode(code);

        if(verificationCode == null ){
            return "invalid";

        }
        User user = verificationCode.getUser();
        Calendar cal = Calendar.getInstance();

        if ((verificationCode.getExpirationTime().getTime()
                - cal.getTime().getTime()) <= 0) {
            verificationCodeRepository.delete(verificationCode);
            return "expired";
        }

        user.setEnabled(true);
        repository.save(user);
        return "valid";


    }

    public AuthenticationResponse getTokensAfterRegistrationVerification(String code){

        VerificationCode verificationCode = verificationCodeRepository.findByCode(code);

        User user = verificationCode.getUser();

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .role(user.getRole())
                .build();

    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .role(user.getRole())
                .build();
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.repository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    public void saveVerificationCodeForUser(String code, User user) {
        VerificationCode verificationCode = new VerificationCode(user,code);
        verificationCodeRepository.save(verificationCode);
    }

    public VerificationCode generateNewVerificationCode(String oldCode) {
        VerificationCode verificationCode = verificationCodeRepository.findByCode(oldCode);
        verificationCode.setCode(UUID.randomUUID().toString());
        verificationCodeRepository.save(verificationCode);
        return verificationCode;
    }

}