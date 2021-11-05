package net.unicon.iam.shibboleth.passwordreset.service.ldap;

import lombok.extern.slf4j.Slf4j;
import net.unicon.iam.shibboleth.passwordreset.service.BasePasswordManagementService;
import net.unicon.iam.shibboleth.passwordreset.service.PasswordManagementService;
import net.unicon.iam.shibboleth.passwordreset.support.ldap.LdapProperties;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import static org.ldaptive.ResultCode.SUCCESS;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.ad.extended.FastBindConnectionInitializer;
import org.ldaptive.extended.ExtendedOperation;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.extended.PasswordModifyRequest;
import org.ldaptive.handler.ResultPredicate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Implementation of {@link PasswordManagementService} for LDAP directory backend.
 */
@Slf4j
public class LdapPasswordManagementService extends BasePasswordManagementService {
    private ConnectionFactory connectionFactory;
    private LdapProperties ldapProperties;

    public LdapPasswordManagementService(LdapProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
        buildConnectionFactory();
    }

    @Override
    public String findEmailAddressFor(String username) {
        Assert.notNull(username, "username cannot be null");
        Optional<SearchResponse> optionalResponse = findSearchResultFor(username, "mail");
        return optionalResponse
                .map(r -> {
                    LdapEntry e = r.getEntry();
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
        return executeGenericLdapPasswordResetOperation(dn, newPassword);
    }

    //Package-private - friendly for unit testing
    String findDnFor(String username) {
        Optional<SearchResponse> optionalResponse = findSearchResultFor(username, "mail");
        return optionalResponse.map(r -> r.getEntry().getDn()).orElse(null);
    }

    Optional<SearchResponse> findSearchResultFor(String username, String... returnAttributes) {
        FilterTemplate filter = buildFilter(ldapProperties.getSearchFilter(), "user", List.of(username));
        return executeSearchOperation(ldapProperties.getBaseDn(), filter, returnAttributes);
    }

    private void buildConnectionFactory() {
        ConnectionConfig config = new ConnectionConfig();
        config.setLdapUrl(ldapProperties.getUrl());
        if (StringUtils.substringMatch(ldapProperties.getBindDn(), 0, "*") &&
            StringUtils.substringMatch(ldapProperties.getBindPassword(), 0, "*")) {
            config.setConnectionInitializers(new FastBindConnectionInitializer());
        } else if (StringUtils.hasText(ldapProperties.getBindDn()) && StringUtils.hasText(ldapProperties.getBindPassword())) {
            config.setConnectionInitializers(new BindConnectionInitializer(ldapProperties.getBindDn(), new Credential(ldapProperties.getBindPassword())));
        }
        connectionFactory = new DefaultConnectionFactory(config);
    }

    FilterTemplate buildFilter(final String filterQuery, final String paramName, final List<String> params) {
        FilterTemplate filter = new FilterTemplate();
        filter.setFilter(filterQuery);
        if (params != null) {
            IntStream.range(0, params.size()).forEach(i -> {
                if (filter.getFilter().contains("{" + i + '}')) {
                    filter.setParameter(i, params.get(i));
                } else {
                    filter.setParameter(paramName, params.get(i));
                }
            });
        }
        log.debug("Constructed LDAP search filter [{}]", filter.format());
        return filter;
    }

    private boolean executeGenericLdapPasswordResetOperation(String dn, String newPassword) {
        log.debug("Executing password reset operation for DN: [{}]", dn);
        try {
            ExtendedOperation passwordModifyOperation = ExtendedOperation.builder().factory(connectionFactory).throwIf(ResultPredicate.NOT_SUCCESS).build();
            ExtendedResponse response = passwordModifyOperation.execute(new PasswordModifyRequest(dn, null, newPassword));
            log.debug("Password reset operation response code: [{}], message: [{}]", response.getResultCode(), response.getDiagnosticMessage());
            return response.getResultCode() == SUCCESS;
        }
        catch (LdapException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    private Optional<SearchResponse> executeSearchOperation(String baseDn, FilterTemplate searchFilter, String... returnAttributes) {
        try {
            SearchRequest req = new SearchRequest(baseDn, searchFilter, returnAttributes);
            SearchOperation op = new SearchOperation(connectionFactory);
            return Optional.ofNullable(op.execute(req));
        }
        catch (LdapException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }
}