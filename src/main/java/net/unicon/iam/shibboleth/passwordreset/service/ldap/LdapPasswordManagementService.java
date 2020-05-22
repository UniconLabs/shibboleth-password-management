package net.unicon.iam.shibboleth.passwordreset.service.ldap;

import net.unicon.iam.shibboleth.passwordreset.service.PasswordManagementService;

/**
 * Implementation of {@link PasswordManagementService} for LDAP directory backend.
 */
public class LdapPasswordManagementService implements PasswordManagementService {

    @Override
    public String findEmailAddressFor(String username) {
        return null;
    }

    @Override
    public String generateResetUrlAndStoreResetTokenFor(String username) {
        return null;
    }

    @Override
    public String findUsernameBy(String resetToken) {
        return null;
    }

    @Override
    public boolean changePasswordFor(String username) {
        return false;
    }
}
