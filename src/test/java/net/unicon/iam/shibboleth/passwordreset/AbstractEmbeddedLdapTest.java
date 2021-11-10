package net.unicon.iam.shibboleth.passwordreset;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import net.unicon.iam.shibboleth.passwordreset.service.LdapPasswordManagementService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Setup the basic embedded LDAP infrastructure to make testing password reset against a real LDAP possible
 */
@ContextConfiguration("/ldap-password-manager-testing.xml")
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AbstractEmbeddedLdapTest {
    protected InetAddress bindAddress;
    protected String bindDSN = "cn=admin,dc=unicon,dc=local";
    protected String[] domainDsnArray = new String[] {"dc=unicon,dc=local"};
    protected List<String> ldifs = new ArrayList<>();
    protected InMemoryDirectoryServer testLdapServer;

    public AbstractEmbeddedLdapTest() {
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

    protected InMemoryDirectoryServerConfig createInMemoryServerConfiguration() {
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

    protected InMemoryDirectoryServer createServer() throws LDAPException {
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