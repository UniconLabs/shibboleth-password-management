package net.unicon.iam.shibboleth.passwordreset.support.ldap;

import lombok.extern.slf4j.Slf4j;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.ad.extended.FastBindOperation;
import org.ldaptive.extended.PasswordModifyOperation;
import org.ldaptive.extended.PasswordModifyRequest;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.ldaptive.ResultCode.SUCCESS;

/**
 * Utility wrapper for Ldaptive library various operations.
 */
@Slf4j
public class Ldaptive {

    public static SearchFilter searchFilterOf(final String filterQuery, final String paramName, final List<String> params) {
        SearchFilter filter = new SearchFilter();
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

    public static ConnectionFactory connectionFactoryOf(LdapProperties ldapProperties) {
        ConnectionConfig config = new ConnectionConfig();
        config.setLdapUrl(ldapProperties.getUrl());
        config.setUseSSL(ldapProperties.isUseSsl());
        if (StringUtils.substringMatch(ldapProperties.getBindDn(), 0, "*")
            && StringUtils.substringMatch(ldapProperties.getBindPassword(), 0, "*")) {
            config.setConnectionInitializer(new FastBindOperation.FastBindConnectionInitializer());
        }
        else if (StringUtils.hasText(ldapProperties.getBindDn()) && StringUtils.hasText(ldapProperties.getBindPassword())) {
            config.setConnectionInitializer(new BindConnectionInitializer(ldapProperties.getBindDn(),
                    new Credential(ldapProperties.getBindPassword())));
        }
        return new DefaultConnectionFactory(config);
    }

    public static Optional<Response<SearchResult>> executeSearchOperation(ConnectionFactory connectionFactory,
                                                                         String baseDn,
                                                                         SearchFilter searchFilter,
                                                                         String... returnAttributes) {

        try(Connection conn = connectionFactory.getConnection()) {
            conn.open();
            SearchRequest req = new SearchRequest(baseDn, searchFilter, returnAttributes);
            SearchOperation op = new SearchOperation(conn);
            return Optional.ofNullable(op.execute(req));
        } catch (LdapException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public static boolean executeGenericLdapPasswordResetOperation(ConnectionFactory connectionFactory, String dn, String newPassword) {
        try (Connection conn = connectionFactory.getConnection()) {
            conn.open();
            log.debug("Executing password reset operation for DN: [{}]", dn);
            PasswordModifyOperation modifyOperation = new PasswordModifyOperation(conn);
            Response<Credential> response = modifyOperation.execute(new PasswordModifyRequest(dn, null, new Credential(newPassword)));
            log.debug("Password reset operation response code: [{}], message: [{}]", response.getResultCode(), response.getMessage());
            return response.getResultCode() == SUCCESS;
        } catch (LdapException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }
}
