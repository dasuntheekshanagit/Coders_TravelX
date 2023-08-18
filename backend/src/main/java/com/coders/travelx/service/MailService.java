package com.coders.travelx.service;




import com.coders.travelx.model.User;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender javaMailSender;

    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public String sendVerifyEmail(User user, String url) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("bidcircleauction@gmail.com");
        message.setTo(user.getEmail());
        String text = "Hi "+ user.getFirstname() + " " +user.getLastname() +"\n\n"
                + "You’re almost ready to get Onboard with TravelX" +"\n"
                +"Verify your email address " + url+"\n\n"+
                "Thanks,"+"\n"
                +"TravelX Team";
        message.setSubject("Verify your Email");
        message.setText(text);
        System.out.println(message);

        javaMailSender.send(message);

        return "Mail sent successfully";
    }


}