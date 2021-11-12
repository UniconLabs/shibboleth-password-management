package net.unicon.iam.shibboleth.passwordreset.token;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is a default, basic implementation, but is NOT recommended for production use. There is no shared resource and all values
 * are volatile.
 */
public class InMemoryTokenRecordStorage implements ITokenRecordStorage {
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