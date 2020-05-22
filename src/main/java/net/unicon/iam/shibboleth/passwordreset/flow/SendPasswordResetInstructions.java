package net.unicon.iam.shibboleth.passwordreset.flow;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.unicon.iam.shibboleth.passwordreset.service.PasswordManagementService;
import net.unicon.iam.shibboleth.passwordreset.support.email.EmailProperties;
import net.unicon.iam.shibboleth.passwordreset.support.email.EmailService;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class SendPasswordResetInstructions extends AbstractProfileAction {

    private EmailProperties emailProperties;

    private EmailService emailService;

    private PasswordManagementService passwordManagementService;

    @Nonnull
    @Override
    protected Event doExecute(@Nonnull RequestContext springRequestContext,
                              @Nonnull ProfileRequestContext profileRequestContext) {

        String username = "TODO:real_impl";
        String emailAddress = this.passwordManagementService.findEmailAddressFor(username);
        if(emailAddress == null) {
            log.error("Could not find password record for [username]");
            return new Event(this, "error");
        }
        String resetUrl = this.passwordManagementService.generateResetUrlAndStoreResetTokenFor(username);

        if(this.emailService.send(this.emailProperties, emailAddress, String.format("Here is your reset URL: %s", resetUrl))) {
            return new Event(this, "success");
        }
        return new Event(this, "error");
    }
}
