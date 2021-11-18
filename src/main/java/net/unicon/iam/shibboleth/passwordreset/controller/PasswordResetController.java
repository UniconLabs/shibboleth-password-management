package net.unicon.iam.shibboleth.passwordreset.controller;

import lombok.extern.slf4j.Slf4j;
import net.unicon.iam.shibboleth.passwordreset.service.IPasswordManagementService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/passwordReset")
@Slf4j
public class PasswordResetController {
    @Autowired
    IPasswordManagementService passwordManagementService;

    /**
     * Return the generated token for the user
     * @param username The username to act upon
     * @return 404 NOTFOUND if the username is not found
     *         200 OK if the token was generated for the user (return body is the token)
     */
    @PostMapping(path = "/initiateResetForUser/{username}")
    public ResponseEntity<?> initiatePasswordReset(@PathVariable String username) {
        if (!passwordManagementService.userExists(username)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user found with username = [" + username + "]");
        }

        String token = passwordManagementService.generateResetTokenFor(username);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    /**
     * @param token the generated token value
     * @param body the password and confirmation password (must match)
     * @return 404 NOT_FOUND if the token does not produce a username
     *         400 BAD_REQUEST if the password or passwordConfirm values supplied are empty(null) or do not match
     *         204 NO_CONTENT if the password is updated successfully
     *
     */
    @PatchMapping(path = "/{token}")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, Object> body, @PathVariable String token) {
        String tokenUser = passwordManagementService.findUsernameBoundToToken(token);
        passwordManagementService.clearToken(token); // Always remove
        if (StringUtils.isEmpty(tokenUser)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No match for token = [" + token + "] found");
        }

        String password = body.get("password").toString();
        String confirmedPassword = body.get("confirmedPassword").toString();
        if(StringUtils.isEmpty(password) || StringUtils.isEmpty(confirmedPassword)) {
            log.error("New password value and confirmed password value must not be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Neither the new password nor the confirmed value may be empty");
        }

        if (!password.equals(confirmedPassword)) {
            log.error("New password value does not match confirmed password value");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The new password and the confirmed password must match");
        }

        if (passwordManagementService.resetPasswordFor(tokenUser, password)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Succesfully changed password for user [" + tokenUser + "]");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to change password for user [" + tokenUser + "]");
    }
}