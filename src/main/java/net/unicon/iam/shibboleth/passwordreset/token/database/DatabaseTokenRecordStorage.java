package net.unicon.iam.shibboleth.passwordreset.token.database;

import lombok.Setter;
import net.unicon.iam.shibboleth.passwordreset.token.ITokenRecordStorage;
import net.unicon.iam.shibboleth.passwordreset.token.database.model.Token;
import net.unicon.iam.shibboleth.passwordreset.token.database.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class DatabaseTokenRecordStorage implements ITokenRecordStorage {
    @Autowired
    private TokenRepository tokenRepository;

    @Override
    @Transactional
    public void bindTokenToUsername(String token, String username) {
        Token entity = new Token();
        entity.setToken(token);
        entity.setUsername(username);
        // Ensure no other token for the user exists first
        tokenRepository.deleteByUsername(username);
        tokenRepository.save(entity);
    }

    @Override
    public String findUsernameBoundToToken(String token) {
        Optional<Token> t = tokenRepository.findByToken(token);
        return t.isPresent() ? t.get().getUsername() : null;
    }

    @Override
    public void removeTokenRecord(String token) {
        tokenRepository.deleteByToken(token);
    }
}