package net.unicon.iam.shibboleth.passwordreset.service.ldap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * These integration tests require Ldap Docker container with fixture data running.
 * Currently using 'directory' container from: https://github.com/UniconLabs/iam-learning-environment
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
        String emailAddressForDimaInLdap = ldapPasswordManagementService.findEmailAddressFor("banderson");
        assertEquals("banderson@mail.com", emailAddressForDimaInLdap);
    }

    @Test
    public void verifyFindDn() {
        String dn = ldapPasswordManagementService.findDnFor("banderson");
        assertEquals("uid=banderson,ou=People,dc=unicon,dc=local", dn);
    }
}
