package net.unicon.iam.shibboleth.passwordreset.flow;

import net.shibboleth.idp.profile.AbstractProfileAction;
import org.opensaml.profile.context.ProfileRequestContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;

public class PasswordResetInit extends AbstractProfileAction {

    @Nonnull
    @Override
    protected Event doExecute(@Nonnull RequestContext springRequestContext, @Nonnull ProfileRequestContext profileRequestContext) {
        String token = springRequestContext.getExternalContext().getRequestParameterMap().get("token");
        springRequestContext.getFlowScope().put("resetToken", token);

        return new Event(this, "success");
    }
}
