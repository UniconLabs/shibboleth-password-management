package net.unicon.iam.shibboleth.passwordreset.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

/**
 * Main API to define operations pertaining to managing passwords i.e.
 * manage reset attempts, store and retrieve data associated with password transactions for different subjects,
 * generate, validate and manage password reset tokens.
 */
public interface PasswordManagementService {

    String findEmailAddressFor(String username);

    String generateResetTokenFor(String username);

    boolean resetPasswordFor(String username, String newPassword);



    @AllArgsConstructor
    @Slf4j
    class MOCK_IMPL implements PasswordManagementService {

        private final String resetBaseUrl;


        @Override
        public String findEmailAddressFor(String username) {
            return "email@gmail.com";
        }

        @Override
        public String generateResetTokenFor(String username) {
            return UUID.randomUUID().toString();
        }

        @Override
        public boolean resetPasswordFor(String username, String newPassword) {
            return false;
        }
    }

}
