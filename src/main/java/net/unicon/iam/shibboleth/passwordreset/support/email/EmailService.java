package net.unicon.iam.shibboleth.passwordreset.support.email;

/**
 * Abstraction modeling sending emails.
 */
public interface EmailService {

    /**
     * Send email.
     *
     * @param emailProperties
     * @param to
     * @param body
     * @return flag indicating whether email was sent or not
     */
    boolean send(EmailProperties emailProperties, String to, String body);


}
