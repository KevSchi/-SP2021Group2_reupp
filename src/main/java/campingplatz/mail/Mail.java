package campingplatz.mail;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;

public class Mail {

    
    public static void sendEMail(Email mail){
              
            Mailer mailer = MailerBuilder
                .withSMTPServer("smtp.gmail.com", 587, "campingplatzgruppe2@gmail.com", "SigUSR123")
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .buildMailer();
                mailer.sendMail(mail);
    }
    
}