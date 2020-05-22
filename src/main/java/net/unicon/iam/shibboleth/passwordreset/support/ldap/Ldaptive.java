package net.unicon.iam.shibboleth.passwordreset.support.ldap;

import lombok.extern.slf4j.Slf4j;
import org.ldaptive.SearchFilter;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Utility wrapper for Ldaptive library various operations.
 */
@Slf4j
public class Ldaptive {

    public static SearchFilter newSearchFilter(final String filterQuery, final String paramName, final List<String> params) {
        SearchFilter filter = new SearchFilter();
        filter.setFilter(filterQuery);
        if (params != null) {
            IntStream.range(0, params.size()).forEach(i -> {
                if (filter.getFilter().contains("{" + i + '}')) {
                    filter.setParameter(i, params.get(i));
                } else {
                    filter.setParameter(paramName, params.get(i));
                }
            });
        }
        log.debug("Constructed LDAP search filter [{}]", filter.format());
        return filter;
    }
}
