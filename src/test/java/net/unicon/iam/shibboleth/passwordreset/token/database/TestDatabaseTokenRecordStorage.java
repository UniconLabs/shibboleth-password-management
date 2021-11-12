package net.unicon.iam.shibboleth.passwordreset.token.database;

import net.unicon.iam.shibboleth.passwordreset.token.database.model.Token;
import net.unicon.iam.shibboleth.passwordreset.token.database.repository.TokenRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testing the public interface ({@link net.unicon.iam.shibboleth.passwordreset.token.ITokenRecordStorage}) for the DB implementation
 */
@DataJpaTest
@ContextConfiguration(classes = TRSConfig.class)
@EnableJpaRepositories("net.unicon.iam.shibboleth.passwordreset.token.database.repository")
@EntityScan("net.unicon.iam.shibboleth.passwordreset.token.database.model")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestDatabaseTokenRecordStorage {
    @Autowired
    private TokenRepository tokenRepo;

    @Autowired
    private DatabaseTokenRecordStorage tokenRecordStorage;

    @BeforeEach
    @Transactional
    public void beforeEach() {
        tokenRepo.deleteAll();
    }

    @Test
    public void test_bindTokenToUsername() {
        Assertions.assertTrue(tokenRepo.count() == 0 );
        tokenRecordStorage.bindTokenToUsername("tokenValue", "username1");
        Assertions.assertTrue(tokenRepo.count() == 1 );
        Assertions.assertEquals("username1", tokenRepo.findByToken("tokenValue").orElse(null).getUsername(), "username incorrect");

        tokenRecordStorage.bindTokenToUsername("newToken", "username1");
        Assertions.assertTrue(tokenRepo.count() == 1 );
        Assertions.assertTrue(tokenRepo.findByToken("tokenValue").isEmpty());
        Assertions.assertEquals("username1", tokenRepo.findByToken("newToken").orElse(null).getUsername(), "username incorrect");
    }

    @Test
    public void test_findUsernameBoundToToken() {
        Token t = new Token();
        t.setToken("tokenVal");
        t.setUsername("username2");
        tokenRepo.saveAndFlush(t);
        Assertions.assertEquals("username2", tokenRecordStorage.findUsernameBoundToToken("tokenVal"), "username incorrct");
        Assertions.assertNull(tokenRecordStorage.findUsernameBoundToToken("ranValue"));
    }

    @Test
    public void test_removeTokenRecord() {
        Assertions.assertTrue(tokenRepo.count() == 0 );
        tokenRecordStorage.removeTokenRecord("randomVal");
        Assertions.assertTrue(tokenRepo.count() == 0 );
        Token t = new Token();
        t.setToken("tokenVal");
        t.setUsername("username2");
        tokenRepo.saveAndFlush(t);
        tokenRecordStorage.removeTokenRecord("randomVal");
        Assertions.assertTrue(tokenRepo.count() == 1 );
        tokenRecordStorage.removeTokenRecord("tokenVal");
        Assertions.assertTrue(tokenRepo.count() == 0 );
    }

    @Test
    public void test_generateTokenFor() {
        Assertions.assertTrue(tokenRepo.count() == 0 );
        String tokenValue = tokenRecordStorage.generateTokenFor("username3");
        Assertions.assertTrue(tokenRepo.count() == 1 );
        Assertions.assertEquals("username3", tokenRecordStorage.findUsernameBoundToToken(tokenValue), "username incorrct");
    }
}