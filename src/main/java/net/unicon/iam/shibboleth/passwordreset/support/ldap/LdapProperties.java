package net.unicon.iam.shibboleth.passwordreset.support.ldap;

import lombok.Data;

@Data
public class LdapProperties {

    private String url;

    private String baseDn;

    private String bindDn;

    private String bindPassword;

    private boolean useSsl;
}
