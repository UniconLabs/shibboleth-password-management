package net.unicon.iam.shibboleth.passwordreset.service.ldap;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ldaptive.FilterTemplate;
import org.ldaptive.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ContextConfiguration("/ldap-password-manager.xml")
@ExtendWith(SpringExtension.class)
public class LdapPasswordManagementServiceTests {
    @Autowired private LdapPasswordManagementService ldapPasswordManagementService;

    private InetAddress bindAddress;
    private String bindDSN = "cn=admin,dc=unicon,dc=local";
    private String[] domainDsnArray = new String[] {"dc=unicon,dc=local"};
    private List<String> ldifs = new ArrayList<>();
    private InMemoryDirectoryServer testLdapServer;

    public LdapPasswordManagementServiceTests() {
        ldifs.add("testusers.ldif");
        try {
            this.bindAddress = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(String.format("Unknown host address \"%s\"", "127.0.0.1"), e);
        }
    }

    @BeforeEach
    public void startEmbeddedLdap() throws LDAPException {
        testLdapServer = createServer();
        testLdapServer.startListening();
    }

    @AfterEach
    public void stopEmbeddedLdap() {
        testLdapServer.shutDown(true);
    }

    @Test
    public void validateChangePassword() throws LDAPException {
        Optional<SearchResponse> originalPass = ldapPasswordManagementService.findSearchResultFor("banderson", "userPassword");
        assertEquals("password", originalPass.map(r -> r.getEntry().getAttribute("userPassword").getStringValue()).orElse(null));

        boolean passwordChanged = ldapPasswordManagementService.resetPasswordFor("banderson", "foobar");
        assertTrue(passwordChanged);

        Optional<SearchResponse> updatedPass = ldapPasswordManagementService.findSearchResultFor("banderson", "userPassword");
        assertEquals("foobar", updatedPass.map(r -> r.getEntry().getAttribute("userPassword").getStringValue()).orElse(null));
    }

    @Test
    public void verifyCreateSearchFilter() throws LDAPException {
        FilterTemplate searchFilter = ldapPasswordManagementService.buildFilter("(uid={user})", "user", List.of("kwhite"));
        String format = searchFilter.format();
        assertEquals("(uid=kwhite)", format);
    }

    @Test
    public void verifyCorrectWiring() {
        assertNotNull(ldapPasswordManagementService);
    }

    @Test
    public void verifyFindDn() throws LDAPException {
        String dn = ldapPasswordManagementService.findDnFor("banderson");
        assertEquals("uid=banderson,ou=People,dc=unicon,dc=local", dn);
    }

    @Test
    public void verifySearchForEmail() throws LDAPException {
        String emailFromLdap = ldapPasswordManagementService.findEmailAddressFor("banderson");
        assertEquals("banderson@mail.com", emailFromLdap);
    }

    private InMemoryDirectoryServerConfig createInMemoryServerConfiguration() {
        try {
            final InMemoryDirectoryServerConfig inMemoryDirectoryServerConfig = new InMemoryDirectoryServerConfig(domainDsnArray);
            inMemoryDirectoryServerConfig.addAdditionalBindCredentials(bindDSN, "admin");
            InMemoryListenerConfig listenerConfig = InMemoryListenerConfig.createLDAPConfig("test-listener", bindAddress, 8389, null);
            inMemoryDirectoryServerConfig.setListenerConfigs(listenerConfig);
            inMemoryDirectoryServerConfig.setSchema(null);
            return inMemoryDirectoryServerConfig;
        }
        catch (LDAPException e) {
            throw new IllegalStateException("Could not create configuration for the in-memory LDAP instance due to an exception", e);
        }
    }

    private InMemoryDirectoryServer createServer() throws LDAPException {
        final InMemoryDirectoryServer ldapServer = new InMemoryDirectoryServer(createInMemoryServerConfiguration());
        for (final String ldif : ldifs) {
            try {
                ldapServer.importFromLDIF(false, URLDecoder.decode(Resources.getResource(ldif).getPath(), Charsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Can not URL decode path:" + Resources.getResource(ldif).getPath(), e);
            }
        }
        return ldapServer;
    }
}