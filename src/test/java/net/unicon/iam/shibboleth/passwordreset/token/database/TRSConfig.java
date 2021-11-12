package net.unicon.iam.shibboleth.passwordreset.token.database;

import net.unicon.iam.shibboleth.passwordreset.token.database.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TRSConfig {
    @Bean
    public DatabaseTokenRecordStorage databaseTokenRecordStorage() {
        DatabaseTokenRecordStorage result = new DatabaseTokenRecordStorage();
        return result;
    }
}