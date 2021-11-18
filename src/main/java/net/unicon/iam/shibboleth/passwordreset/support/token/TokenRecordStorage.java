package net.unicon.iam.shibboleth.passwordreset.support.token;

import java.util.Map;
import java.util.UUID;
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

    default String generateTokenFor(String username) {
        String token = UUID.randomUUID().toString();
        bindTokenToUsername(token, username);
        return token;
    }

    /**
     * This is a default, basic implementation, but is NOT recommended for production use.
     */
    class IN_MEMORY implements TokenRecordStorage {
        protected Map<String, String> map = new ConcurrentHashMap<>();

        @Override
        public void bindTokenToUsername(String token, String username) {
            AtomicReference<String> removeKey = new AtomicReference<>("");
            map.forEach((key, value) -> {
                if (value.equals(username)) {
                    removeKey.set(key);
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