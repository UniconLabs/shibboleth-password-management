package net.unicon.iam.shibboleth.passwordreset.support;

/**
 * Model class for email-related properties
 */
public class EmailProperties {

    private String from;

    private String subject;

    private boolean html = false;

    private boolean validateAddresses = false;

    private String cc;

    private String bcc;
}
