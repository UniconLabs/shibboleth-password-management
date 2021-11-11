package net.unicon.iam.shibboleth.passwordreset.support.token;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public interface TokenRecordStorage {
    /**
     * A user can only have one token at a time, implementations need to track and remove any previously associated token before binding
     * the supplied token with the user
     * @param token The value to bind to the user
     * @param username The name of the user
     */
    void bindTokenToUsername(String token, String username);

    String findUsernameBoundToToken(String token);

    void removeTokenRecord(String token);

    /**
     * This is a default, basic implementation, but is NOT reccomended for production use.
     */
    class IN_MEMORY implements TokenRecordStorage {
        protected Map<String, String> map = new ConcurrentHashMap<>();

        @Override
        public void bindTokenToUsername(String token, String username) {
            AtomicReference<String> removeKey = new AtomicReference<>("");
            map.entrySet().forEach(entry -> {
                if (entry.getValue().equals(username)) {
                    removeKey.set(entry.getKey());
                }
            });
            map.remove(removeKey.get());
            map.put(token, username);
        }

        @Override
        public String findUsernameBoundToToken(String token) {
            synchronized (map) {
                return map.get(token);
            }
        }

        @Override
        public void removeTokenRecord(String token) {
            map.remove(token);
        }
    }
}