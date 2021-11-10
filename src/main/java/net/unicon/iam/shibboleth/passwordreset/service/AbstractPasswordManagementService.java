package net.unicon.iam.shibboleth.passwordreset.service;

import lombok.extern.slf4j.Slf4j;
import net.unicon.iam.shibboleth.passwordreset.support.email.EmailProperties;
import net.unicon.iam.shibboleth.passwordreset.support.email.EmailService;
import net.unicon.iam.shibboleth.passwordreset.support.token.TokenRecordStorage;

import java.util.UUID;

/**
 * Provides common functionality.
 */
@Slf4j
public abstract class AbstractPasswordManagementService implements IPasswordManagementService {
    protected String baseUrl;
    protected EmailProperties emailProperties;
    protected EmailService emailService;
    protected TokenRecordStorage tokenRecordStorage = new TokenRecordStorage.IN_MEMORY();

    @Override
    public void clearToken(String token) {
        tokenRecordStorage.removeTokenRecord(token);
    }

    @Override
    public String generateResetTokenFor(String username) {
        return UUID.randomUUID().toString();
    }

    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    public void setEmailProperties(EmailProperties props) {
        this.emailProperties = props;
    }

    public void setEmailService(EmailService service) {
        this.emailService = service;
    }

    public void setTokenRecordStorage(TokenRecordStorage storage) {
        this.tokenRecordStorage = storage;
    }
}