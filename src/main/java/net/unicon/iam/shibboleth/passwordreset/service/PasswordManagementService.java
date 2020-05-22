package net.unicon.iam.shibboleth.passwordreset.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.unicon.iam.shibboleth.passwordreset.model.PasswordResetRequest;
import org.opensaml.storage.StorageRecord;
import org.opensaml.storage.StorageService;

import java.io.IOException;
import java.util.UUID;

/**
 * Main API to define operations pertaining to managing passwords i.e.
 * manage reset attempts, store and retrieve data associated with password transactions for different subjects,
 * generate, validate and manage password reset tokens.
 */
public interface PasswordManagementService {

    String findEmailAddressFor(String username);

    String generateResetUrlAndStoreResetTokenFor(String username);

    String findUsernameBy(String resetToken);

    boolean changePasswordFor(String username);

    String PASSWORD_RESET_CONTEXT = "passwordreset";

    @AllArgsConstructor
    @Slf4j
    class MOCK_IMPL implements PasswordManagementService {

        private String resetBaseUrl;

        private StorageService storageService;


        @Override
        public String findEmailAddressFor(String username) {
            return "email@gmail.com";
        }

        @Override
        public String generateResetUrlAndStoreResetTokenFor(String username) {
            String token = UUID.randomUUID().toString();
            try {
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

        @Override
        public boolean changePasswordFor(String username) {
            return false;
        }
    }

}
