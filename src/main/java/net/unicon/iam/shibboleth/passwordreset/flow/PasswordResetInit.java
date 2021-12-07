package net.unicon.iam.shibboleth.passwordreset.flow;

import lombok.extern.slf4j.Slf4j;
import net.unicon.iam.shibboleth.passwordreset.token.ITokenRecordStorage;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;

@Slf4j
public class PasswordResetInit extends AbstractAction {

    private final ITokenRecordStorage tokenRecordStorage;


    public PasswordResetInit(ITokenRecordStorage tokenRecordStorage) {
        this.tokenRecordStorage = tokenRecordStorage;
        log.debug("Creating new instance...");
        log.debug("Token storage hashcode" + tokenRecordStorage.hashCode());

    }

    @Nonnull
    @Override
    protected Event doExecute(@Nonnull RequestContext springRequestContext) {
        var token = springRequestContext.getExternalContext().getRequestParameterMap().get("token");
        log.info("Received token: [{}]", token);

        //TODO: Should we think about expiration concept for these tokens also?
        var username = tokenRecordStorage.findUsernameBoundToToken(token);
        if (username == null) {
            log.error("Token record not found for token [{}]", token);
            return new Event(this, "error");
        }
        log.info("Found username [{}] bound to reset token [{}]", username, token);
        log.debug("Removing token [{}] record for username [{}]", token, username);
        tokenRecordStorage.removeTokenRecord(token);
        log.debug("Putting username [{}] into flow scope...", username);
        springRequestContext.getFlowScope().put(FlowKeyNames.PASSWD_RESET_TX_IN_PROGRESS_USER, username);

        return new Event(this, "success");
    }
}