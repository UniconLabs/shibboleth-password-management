package net.unicon.iam.shibboleth.passwordreset.service;

import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

/**
 * Provides common functionality.
 */
@Slf4j
public abstract class BasePasswordManagementService implements PasswordManagementService {

    @Override
    public String generateResetTokenFor(String username) {
        return UUID.randomUUID().toString();
    }
}
