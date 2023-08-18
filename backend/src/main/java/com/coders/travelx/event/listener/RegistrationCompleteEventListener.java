package com.coders.travelx.event.listener;

import com.coders.travelx.auth.AuthenticationService;
import com.coders.travelx.event.RegistrationCompleteEvent;
import com.coders.travelx.model.User;
import com.coders.travelx.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RegistrationCompleteEventListener implements
        ApplicationListener<RegistrationCompleteEvent> {

   private final AuthenticationService authenticationService;



    private final MailService mailService;

    public RegistrationCompleteEventListener(AuthenticationService authenticationService, MailService mailService) {
        this.authenticationService = authenticationService;
        this.mailService = mailService;
    }

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        //Create the Verification Token for the User with Link
        User user = event.getUser();
        String code = UUID.randomUUID().toString();
        authenticationService.saveVerificationCodeForUser(code,user);
        //Send Mail to user
        String url =
                event.getApplicationUrl()
                        + "/api/v1/auth/verifyRegistration?code="
                        + code;

        mailService.sendVerifyEmail(user, url);
    }
}
