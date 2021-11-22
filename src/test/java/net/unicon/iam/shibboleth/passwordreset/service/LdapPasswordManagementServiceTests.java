package net.unicon.iam.shibboleth.passwordreset.service;

import net.unicon.iam.shibboleth.passwordreset.AbstractEmbeddedLdapTest;
import org.junit.jupiter.api.Test;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LdapPasswordManagementServiceTests extends AbstractEmbeddedLdapTest {
    @Autowired
    private LdapPasswordManagementService ldapPasswordManagementService;

    @Test
    public void validateChangePassword() {
        Optional<Response<SearchResult>> originalPass = ldapPasswordManagementService.findSearchResultFor("banderson", "userPassword");
        assertEquals("password", originalPass.map(r -> r.getResult().getEntry().getAttribute("userPassword").getStringValue()).orElse(null));

        boolean passwordChanged = ldapPasswordManagementService.resetPasswordFor("banderson", "foobar");
        assertTrue(passwordChanged);

        Optional<Response<SearchResult>> updatedPass = ldapPasswordManagementService.findSearchResultFor("banderson", "userPassword");
        assertEquals("foobar", updatedPass.map(r -> r.getResult().getEntry().getAttribute("userPassword").getStringValue()).orElse(null));
    }

    @Test
    public void verifyCreateSearchFilter() {
        SearchFilter searchFilter = ldapPasswordManagementService.buildFilter("(uid={user})", "user", List.of("kwhite"));
        String format = searchFilter.format();
        assertEquals("(uid=kwhite)", format);
    }

    @Test
    public void verifyCorrectWiring() {
        assertNotNull(ldapPasswordManagementService);
    }

    @Test
    public void verifyFindDn() {
        String dn = ldapPasswordManagementService.findDnFor("banderson");
        assertEquals("uid=banderson,ou=People,dc=unicon,dc=local", dn);
    }

    @Test
    public void verifySearchForEmail() {
        String emailFromLdap = ldapPasswordManagementService.findEmailAddressFor("banderson");
        assertEquals("banderson@mail.com", emailFromLdap);
    }
}