package net.unicon.iam.shibboleth.passwordreset.flow;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.idp.profile.AbstractProfileAction;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.storage.StorageRecord;
import org.opensaml.storage.StorageService;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Nonnull;
import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class PasswordResetInit extends AbstractProfileAction {

    private StorageService storageService;

    @Nonnull
    @Override
    protected Event doExecute(@Nonnull RequestContext springRequestContext, @Nonnull ProfileRequestContext profileRequestContext) {
        String token = springRequestContext.getExternalContext().getRequestParameterMap().get("token");
        String storedToken = null;
        log.info("Received token: [{}]", token);
        try {
            StorageRecord<String> storageRecord = this.storageService.read("passwordreset", "token");
            if(storageRecord == null) {
                return new Event(this, "error");
            }
            storedToken = storageRecord.getValue();
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
            return new Event(this, "error");
        }
        log.info("Stored token is [{}]", storedToken);

        springRequestContext.getFlowScope().put("resetToken", token);

        return new Event(this, "success");
    }
}
