package net.unicon.iam.shibboleth.passwordreset.plugin;

import net.shibboleth.idp.plugin.PluginException;
import net.shibboleth.idp.plugin.PropertyDrivenIdPPlugin;

import java.io.IOException;

public class PasswordResetPlugin extends PropertyDrivenIdPPlugin {

    public PasswordResetPlugin() throws PluginException, IOException {
        super(PasswordResetPlugin.class);
    }
}