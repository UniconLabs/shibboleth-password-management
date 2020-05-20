package net.unicon.iam.shibboleth.passwordreset.support;

/**
 * Implementation of {@link EmailService} based on Spring's MailSender.
 */
public class SpringMailSenderEmailService implements EmailService {



    @Override
    public boolean send(EmailProperties emailProperties, String to, String body) {
        return false;
    }
}
