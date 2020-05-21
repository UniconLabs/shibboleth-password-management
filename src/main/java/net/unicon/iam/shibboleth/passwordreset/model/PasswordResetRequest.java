package net.unicon.iam.shibboleth.passwordreset.model;

import lombok.Data;

@Data
public class PasswordResetRequest {

    private String username;

    private String newPassword;

    private String newPassworrdConfirmation;
}
