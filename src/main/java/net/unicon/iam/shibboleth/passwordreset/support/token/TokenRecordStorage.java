package net.unicon.iam.shibboleth.passwordreset.support.token;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface TokenRecordStorage {

    void bindTokenToUsername(String token, String username);

    String findUsernameBoundToToken(String token);

    void removeTokenRecord(String token);

    class IN_MEMORY implements TokenRecordStorage {
        final private Map<String, String> m = new ConcurrentHashMap<>();

        @Override
        public void bindTokenToUsername(String token, String username) {
            m.put(token, username);
        }

        @Override
        public String findUsernameBoundToToken(String token) {
            synchronized (m) {
                return m.get(token);
            }

        }

        @Override
        public void removeTokenRecord(String token) {
            m.remove(token);
        }
    }
}
