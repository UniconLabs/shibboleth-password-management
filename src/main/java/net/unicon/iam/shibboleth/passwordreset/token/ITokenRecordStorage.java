package net.unicon.iam.shibboleth.passwordreset.token;

import java.util.UUID;

public interface ITokenRecordStorage {
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

}