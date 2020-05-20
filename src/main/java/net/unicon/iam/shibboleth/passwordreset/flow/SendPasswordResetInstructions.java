package net.unicon.iam.shibboleth.passwordreset.flow;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.unicon.iam.shibboleth.passwordreset.support.EmailProperties;
import net.unicon.iam.shibboleth.passwordreset.support.EmailService;
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

    @Nonnull
    @Override
    protected Event doExecute(@Nonnull RequestContext springRequestContext, @Nonnull ProfileRequestContext profileRequestContext) {
        if(this.emailService.send(this.emailProperties, "test@email.com", "Hooray, that works!")) {
            return new Event(this, "success");
        }
        return new Event(this, "error");
    }
}
