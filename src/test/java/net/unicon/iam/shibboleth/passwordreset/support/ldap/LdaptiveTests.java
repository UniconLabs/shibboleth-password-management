package net.unicon.iam.shibboleth.passwordreset.support.ldap;


import org.junit.jupiter.api.Test;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LdaptiveTests {

    @Test
    public void verifyCreateSearchFilter() {
        SearchFilter searchFilter = Ldaptive.searchFilterOf("(uid={user})", "user", List.of("dima"));
        String format = searchFilter.format();
        assertEquals("(uid=dima)", format);
    }


    //@Test
    public void temp() {
        LdapProperties p = new LdapProperties();
        p.setUrl("ldap://localhost:10389");
        p.setBaseDn("ou=people,dc=example,dc=com");
        p.setBindDn("cn=admin");
        p.setBindPassword("password");

        ConnectionFactory connFactory = Ldaptive.connectionFactoryOf(p);
        SearchFilter filter = Ldaptive.searchFilterOf("(uid={user})", "user", List.of("dima"));

        Optional<Response<SearchResult>> optRes =
                Ldaptive.executeSearchOperation(connFactory, p.getBaseDn(), filter, "mail");

        optRes.ifPresent(r -> {
            String mail = r.getResult().getEntry().getAttribute().getStringValue();
            String dn = r.getResult().getEntry().getDn();
            boolean changed = Ldaptive.executeGenericLdapPasswordResetOperation(connFactory, dn, "newPassword");
        });


    }
}
