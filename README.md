Password reset plugin for Shibboleth Idp v4+
===

The flow config files and the views are self-contained and embedded in the plugin jar's classpath `META-INF` location, specifically not under `net.shibboleth` namespace, but rather under custom `net.unicon` namespace. To tell Shibboleth where to find those, these properties and bean configs are used:

* In `IDP_HOME/conf/idp.properties`: `idp.views = classpath*:/META-INF/net/shibboleth/idp/views,classpath*:/META-INF/net/unicon/idp/views,%{idp.home}/views`

* In `IDP_HOME/system/conf/webflow-config.xml - shibboleth.DefaultFlowPatterns bean`:

<!--Customizing for specific plugins not to be loaded from net.shibboleth namespace -->
<entry key="classpath*:/META-INF/net/unicon/idp/flows/**/*-flow.xml"
            value="/META-INF/net/unicon/idp/flows" />


Pasword reset specific properties are set in `IDP_HOME/conf/password-reset.properties`


