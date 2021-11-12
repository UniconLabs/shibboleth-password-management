package net.unicon.iam.shibboleth.passwordreset.token.database.repository;

import net.unicon.iam.shibboleth.passwordreset.token.database.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    void deleteByToken(String token);

    void deleteByUsername(String username);

    Optional<Token> findByToken(String token);
}