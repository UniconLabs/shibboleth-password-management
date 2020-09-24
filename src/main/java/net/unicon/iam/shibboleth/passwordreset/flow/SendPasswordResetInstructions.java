package net.unicon.iam.shibboleth.passwordreset.flow;

import lombok.extern.slf4j.Slf4j;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.unicon.iam.shibboleth.passwordreset.service.PasswordManagementService;
import net.unicon.iam.shibboleth.passwordreset.support.email.EmailProperties;
import net.unicon.iam.shibboleth.passwordreset.support.email.EmailService;
import net.unicon.iam.shibboleth.passwordreset.support.token.TokenRecordStorage;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.storage.StorageService;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;

@Slf4j
public class SendPasswordResetInstructions extends AbstractAction {

    private final EmailProperties emailProperties;

    private final EmailService emailService;

    private final PasswordManagementService passwordManagementService;

    private final String resetBaseUrl;

    private final TokenRecordStorage tokenRecordStorage;

    public SendPasswordResetInstructions(EmailProperties emailProperties,
                                         EmailService emailService,
                                         PasswordManagementService passwordManagementService,
                                         String resetBaseUrl,
                                         TokenRecordStorage tokenRecordStorage) {

        this.emailProperties = emailProperties;
        this.emailService = emailService;
        this.passwordManagementService = passwordManagementService;
        this.resetBaseUrl = resetBaseUrl;
        this.tokenRecordStorage = tokenRecordStorage;
    }

    @Nonnull
    @Override
    protected Event doExecute(@Nonnull RequestContext springRequestContext) {
        var username = springRequestContext.getRequestParameters().get("username");
        var emailAddress = this.passwordManagementService.findEmailAddressFor(username);
        if(emailAddress == null) {
            log.error("Could not find password record for [{}]", username);
            return new Event(this, "error");
        }
        var token = this.passwordManagementService.generateResetTokenFor(username);
        log.debug("Binding reset token [{}} to username [{}]", token, username);
        tokenRecordStorage.bindTokenToUsername(token, username);
        var resetUrl = String.format(resetBaseUrl + "%s", token);
        log.debug("Generated reset URL: [{}]", resetUrl);

        if(this.emailService.send(this.emailProperties, emailAddress, String.format("Here is your reset URL: %s", resetUrl))) {
            return new Event(this, "success");
        }
        return new Event(this, "error");
    }
}
