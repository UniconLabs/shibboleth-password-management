package net.unicon.iam.shibboleth.passwordreset.flow;

import lombok.extern.slf4j.Slf4j;
import net.unicon.iam.shibboleth.passwordreset.service.PasswordManagementService;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Slf4j
public class PerformPasswordReset extends AbstractAction {

    private PasswordManagementService passwordManagementService;

    public PerformPasswordReset(PasswordManagementService passwordManagementService) {
        this.passwordManagementService = passwordManagementService;
    }

    @Nonnull
    @Override
    protected Event doExecute(@Nonnull RequestContext springRequestContext) {
        //TODO: possibly introduce password validator
        var username = springRequestContext.getFlowScope().getRequiredString(FlowKeyNames.PASSWD_RESET_TX_IN_PROGRESS_USER);
        var newPassword = springRequestContext.getRequestParameters().getRequired("password");
        var confirmedPassword = springRequestContext.getRequestParameters().get("confirmpassword");
        if(StringUtils.isEmpty(newPassword) || StringUtils.isEmpty(confirmedPassword)) {
            log.error("New password value and confirmed password value must not be empty");
            return new Event(this, "error");
        }
        if (!newPassword.equals(confirmedPassword)) {
            log.error("New password value does not match confirmed password value");
            return new Event(this, "error");
        }
        log.debug("Resetting password for user [{}]...", username);
        var isSuccess = passwordManagementService.resetPasswordFor(username, newPassword);

        return new Event(this, isSuccess ? "success" : "error");
    }
}
