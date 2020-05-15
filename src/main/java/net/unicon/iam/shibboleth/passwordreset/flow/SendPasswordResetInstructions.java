package net.unicon.iam.shibboleth.passwordreset.flow;

import net.shibboleth.idp.profile.AbstractProfileAction;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;

public class SendPasswordResetInstructions extends AbstractProfileAction {

    @Nonnull
    @Override
    protected Event doExecute(@Nonnull RequestContext springRequestContext, @Nonnull ProfileRequestContext profileRequestContext) {
        return new Event(this, "success");
    }
}
