package net.unicon.iam.shibboleth.passwordreset.service.ldap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.unicon.iam.shibboleth.passwordreset.service.PasswordManagementService;
import net.unicon.iam.shibboleth.passwordreset.support.ldap.LdapProperties;
import net.unicon.iam.shibboleth.passwordreset.support.ldap.Ldaptive;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of {@link PasswordManagementService} for LDAP directory backend.
 */
@Slf4j
@RequiredArgsConstructor
public class LdapPasswordManagementService implements PasswordManagementService {

    private final LdapProperties ldapProperties;

    @Override
    public String findEmailAddressFor(String username) {
        Assert.notNull(username, "username cannot be null");
        final String emailToReturn;

        ConnectionFactory connFactory = Ldaptive.connectionFactoryOf(ldapProperties);
        SearchFilter filter = Ldaptive.searchFilterOf(ldapProperties.getSearchFilter(), "user", List.of(username));
        Optional<Response<SearchResult>> optionalResponse =
                Ldaptive.executeSearchOperation(connFactory, ldapProperties.getBaseDn(), filter, "mail");

        return optionalResponse
                .map(r -> r.getResult().getEntry().getAttribute().getStringValue())
                .orElse(null);
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
