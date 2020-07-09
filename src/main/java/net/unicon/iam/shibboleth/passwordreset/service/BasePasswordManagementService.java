package net.unicon.iam.shibboleth.passwordreset.service;

import lombok.extern.slf4j.Slf4j;
import org.opensaml.storage.StorageRecord;
import org.opensaml.storage.StorageService;

import java.io.IOException;
import java.util.UUID;

/**
 * Provides common functionality.
 */
@Slf4j
public abstract class BasePasswordManagementService implements PasswordManagementService {

    private final String resetBaseUrl;

    private final StorageService storageService;

    public BasePasswordManagementService(String resetBaseUrl, StorageService storageService) {
        this.resetBaseUrl = resetBaseUrl;
        this.storageService = storageService;
    }

    @Override
    public String generateResetUrlAndStoreResetTokenFor(String username) {
        String token = UUID.randomUUID().toString();
        try {
            //TODO: should we consider expiration?
            storageService.create(PASSWORD_RESET_CONTEXT, token, username, null);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
        return String.format(resetBaseUrl + "%s", token);
    }

    @Override
    public String findUsernameBy(String resetToken) {
        try {
            StorageRecord<String> storageRecord = storageService.read(PASSWORD_RESET_CONTEXT, resetToken);
            return storageRecord == null ? null : storageRecord.getValue();
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
