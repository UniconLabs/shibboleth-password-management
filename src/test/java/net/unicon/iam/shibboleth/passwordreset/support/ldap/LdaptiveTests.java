package net.unicon.iam.shibboleth.passwordreset.support.ldap;


import org.junit.jupiter.api.Test;
import org.ldaptive.SearchFilter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LdaptiveTests {

    @Test
    public void verifyCreateSearchFilter() {
        SearchFilter searchFilter = Ldaptive.newSearchFilter("(uid={user})", "user", List.of("dima"));
        String format = searchFilter.format();
        assertEquals("(uid=dima)", format);
    }
}
