package net.unicon.iam.shibboleth.passwordreset.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

/**
 * Main API to define operations pertaining to managing passwords i.e.
 * manage reset attempts, store and retrieve data associated with password transactions for different subjects,
 * generate, validate and manage password reset tokens.
 */
public interface IPasswordManagementService {

    void clearToken(String token);

    String findEmailAddressFor(String username);

    String findUsernameBoundToToken(String token);

    String generateResetTokenFor(String username);

    boolean resetPasswordFor(String username, String newPassword);

    boolean sendPasswordResetEmail(String username, String emailAddress);

    boolean userExists(String username);

}