package net.unicon.iam.shibboleth.passwordreset.flow;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.unicon.iam.shibboleth.passwordreset.support.EmailProperties;
import net.unicon.iam.shibboleth.passwordreset.support.EmailService;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.storage.StorageService;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class SendPasswordResetInstructions extends AbstractProfileAction {

    private EmailProperties emailProperties;

    private EmailService emailService;

    private StorageService storageService;

    @Nonnull
    @Override
    protected Event doExecute(@Nonnull RequestContext springRequestContext,
                              @Nonnull ProfileRequestContext profileRequestContext) {
        String token = UUID.randomUUID().toString();

        try {
            this.storageService.create("passwordreset", "token", token, null);
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
            return new Event(this, "error");
        }


        if(this.emailService.send(this.emailProperties, "test@email.com", String.format("Here is your reset token: %s", token))) {
            return new Event(this, "success");
        }
        return new Event(this, "error");
    }
}
