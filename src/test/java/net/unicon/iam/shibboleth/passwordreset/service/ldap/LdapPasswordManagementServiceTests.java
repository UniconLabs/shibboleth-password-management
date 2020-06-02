package net.unicon.iam.shibboleth.passwordreset.service.ldap;

import net.unicon.iam.shibboleth.passwordreset.service.PasswordManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * These integration tests require Ldap Docker container with fixture data running
 */
@ContextConfiguration("/ldap-password-manager.xml")
@ExtendWith(SpringExtension.class)
public class LdapPasswordManagementServiceTests {

    @Autowired
    private LdapPasswordManagementService ldapPasswordManagementService;

    @Test
    public void verifyCorrectWiring() {
        assertNotNull(ldapPasswordManagementService);
    }

    @Test
    public void verifySearchForEmail() {
        String emailAddressForDimaInLdap = ldapPasswordManagementService.findEmailAddressFor("dima");
        assertEquals("dima@gmail.com", emailAddressForDimaInLdap);
    }

    @Test
    public void verifyFindDn() {
        String dn = ldapPasswordManagementService.findDnFor("dima");
        assertEquals("uid=dima,ou=People,dc=example,dc=com", dn);
    }
}
