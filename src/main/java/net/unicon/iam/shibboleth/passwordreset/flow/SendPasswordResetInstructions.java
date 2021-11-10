package net.unicon.iam.shibboleth.passwordreset.flow;

import lombok.extern.slf4j.Slf4j;
import net.unicon.iam.shibboleth.passwordreset.service.IPasswordManagementService;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;

@Slf4j
public class SendPasswordResetInstructions extends AbstractAction {
    private final IPasswordManagementService passwordManagementService;

    public SendPasswordResetInstructions(IPasswordManagementService passwordManagementService) {
        this.passwordManagementService = passwordManagementService;
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

        boolean sent = passwordManagementService.sendPasswordResetEmail(username, emailAddress);
        return new Event(this, sent ? "success" : "error");
    }
}