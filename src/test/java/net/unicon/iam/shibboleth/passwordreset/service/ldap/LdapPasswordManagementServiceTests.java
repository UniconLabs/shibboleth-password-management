package net.unicon.iam.shibboleth.passwordreset.service.ldap;

import net.unicon.iam.shibboleth.passwordreset.service.PasswordManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration("/ldap-password-manager.xml")
@ExtendWith(SpringExtension.class)
public class LdapPasswordManagementServiceTests {

    @Autowired
    private PasswordManagementService ldapPasswordManagementService;

    @Test
    public void verifyCorrectWiring() {
        assertNotNull(ldapPasswordManagementService);
    }


    /**
     * These integration tests require Ldap container with fixture data running
     */
    @Test
    public void verifySearchForEmail() {
        String emailAddressForDimaInLdap = ldapPasswordManagementService.findEmailAddressFor("dima");
        assertEquals("dima@gmail.com", emailAddressForDimaInLdap);
    }
}
