package net.unicon.iam.shibboleth.passwordreset.token.database;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TRSConfig {
    @Bean
    public RdbmsTokenRecordStorage databaseTokenRecordStorage() {
        RdbmsTokenRecordStorage result = new RdbmsTokenRecordStorage();
        return result;
    }
}