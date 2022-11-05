package com.G2T7.OurGardenStory.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

import java.io.IOException;

@Service
public class MailService {

    @Value(value = "${sendgrid.api.key}")
    private String SENDGRID_API_KEY;

    public String sendTextEmail (String emailTo, String status) throws IOException {
        // the sender email should be the same as we used to Create a Single Sender Verification
        Email from = new Email("jinhan.loh.2021@scis.smu.edu.sg");
        String subject = "OurGardenStory Ballot";
        Email to = new Email(emailTo);
        String message = "";
        if (status.equals("SUCCESS")) {
            message = "Congratulations, your ballot was successful :)";
        } else if (status.equals("FAIL")) {
            message = "Sorry, your ballot was unsuccessful :(";
        }
        Content content = new Content("text/plain", message);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            return response.getBody();
        } catch (IOException ex) {
            throw ex;
        }
    }
}
