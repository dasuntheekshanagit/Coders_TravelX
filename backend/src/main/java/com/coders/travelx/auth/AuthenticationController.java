package com.coders.travelx.auth;

import com.coders.travelx.event.RegistrationCompleteEvent;
import com.coders.travelx.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final ApplicationEventPublisher publisher;

    @PostMapping("/register")
    public String register(
            @RequestBody RegisterRequest request,
            final HttpServletRequest webRequest

    ) {

        User user;
    try{
        user = service.register(request);
    }
    catch (IllegalArgumentException e){
        return "Email Already Exists";
    }


        publisher.publishEvent(new RegistrationCompleteEvent(user,applicationUrl(webRequest)));
        return "Registration Successfull - Verification Email Sent";
    }

    @GetMapping("/verifyRegistration")
    public ResponseEntity<?> verifyRegistration(@RequestParam("code") String code) {
        String result = service.validateVerificationCode(code);

        if (result.equalsIgnoreCase("valid")) {
            // Call your authentication service to obtain AuthenticationResponse
            AuthenticationResponse authResponse = service.getTokensAfterRegistrationVerification(code);
            return ResponseEntity.ok(authResponse);
        } else if (result.equalsIgnoreCase("expired")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Verification link has expired");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid User");
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" +
                request.getServerName() +
                ":" +
                request.getServerPort() +
                request.getContextPath();
    }


}