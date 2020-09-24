package net.unicon.iam.shibboleth.passwordreset.service.ldap;

import lombok.extern.slf4j.Slf4j;
import net.unicon.iam.shibboleth.passwordreset.service.BasePasswordManagementService;
import net.unicon.iam.shibboleth.passwordreset.service.PasswordManagementService;
import net.unicon.iam.shibboleth.passwordreset.support.ldap.LdapProperties;
import net.unicon.iam.shibboleth.passwordreset.support.ldap.Ldaptive;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link PasswordManagementService} for LDAP directory backend.
 */
@Slf4j
public class LdapPasswordManagementService extends BasePasswordManagementService {

    private final LdapProperties ldapProperties;

    //Is ConnectionFactory thread-safe?
    private final ConnectionFactory connectionFactory;

    public LdapPasswordManagementService(LdapProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
        this.connectionFactory = Ldaptive.connectionFactoryOf(ldapProperties);
    }

    @Override
    public String findEmailAddressFor(String username) {
        Assert.notNull(username, "username cannot be null");
        Optional<Response<SearchResult>> optionalResponse = findSearchResultFor(username, "mail");
        return optionalResponse
                .map(r -> {
                    LdapEntry e = r.getResult().getEntry();
                    LdapAttribute attr = e == null ? null : e.getAttribute();
                    if (attr == null) {
                        log.warn("Cannot find 'mail' attribute for [{}]", username);
                        return null;
                    }
                    return attr.getStringValue();
                })
                .orElse(null);
    }

    @Override
    public boolean resetPasswordFor(String username, String newPassword) {
        log.debug("Executing LDAP password reset operation for user [{}]", username);
        String dn = findDnFor(username);
        if (dn == null) {
            log.warn("The LDAP entry is not found for [{}]. Unable to reset password", username);
            return false;
        }
        return Ldaptive.executeGenericLdapPasswordResetOperation(connectionFactory, dn, newPassword);
    }

    //Package-private - friendly for unit testing
    String findDnFor(String username) {
        Optional<Response<SearchResult>> optionalResponse = findSearchResultFor(username, "mail");
        return optionalResponse
                .map(r -> r.getResult().getEntry().getDn())
                .orElse(null);
    }

    private Optional<Response<SearchResult>> findSearchResultFor(String username, String... returnAttributes) {
        SearchFilter filter = Ldaptive.searchFilterOf(ldapProperties.getSearchFilter(), "user", List.of(username));
        return Ldaptive.executeSearchOperation(connectionFactory, ldapProperties.getBaseDn(), filter, returnAttributes);
    }
}
