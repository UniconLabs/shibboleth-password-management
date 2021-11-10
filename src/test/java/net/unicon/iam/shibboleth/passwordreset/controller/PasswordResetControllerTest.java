package net.unicon.iam.shibboleth.passwordreset.controller;

import net.unicon.iam.shibboleth.passwordreset.AbstractEmbeddedLdapTest;
import net.unicon.iam.shibboleth.passwordreset.service.AbstractPasswordManagementService;
import net.unicon.iam.shibboleth.passwordreset.support.email.EmailProperties;
import net.unicon.iam.shibboleth.passwordreset.support.email.EmailService;
import net.unicon.iam.shibboleth.passwordreset.support.token.TokenRecordStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;

public class PasswordResetControllerTest extends AbstractEmbeddedLdapTest {
    @Autowired
    private EmailProperties emailProperties;

    @Autowired
    private AbstractPasswordManagementService passwordManagementService;

    private EmailService emailService;
    private MockMvc mockMvc;
    private PasswordResetController controller;
    private TestTRS tokenRecordStorage =  new TestTRS();

    @BeforeAll
    public void setup() {
        controller = new PasswordResetController();
        emailService = Mockito.mock(EmailService.class);
        passwordManagementService.setEmailService(emailService);
        passwordManagementService.setEmailProperties(emailProperties);
        passwordManagementService.setTokenRecordStorage(tokenRecordStorage);
        controller.passwordManagementService = passwordManagementService;
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void test_initiateResetForUser() throws Exception {
        tokenRecordStorage.clear();
        // easy path
        Mockito.when(emailService.send(eq(emailProperties), eq("banderson@mail.com"), Mockito.anyString())).thenReturn(true);
        var result =  mockMvc.perform(post("/api/passwordReset/initiateResetForUser/banderson"));//.contentType(APPLICATION_JSON).content("postedJsonBody"));
        result.andExpect(status().isOk());
        Assertions.assertEquals(1, tokenRecordStorage.getTokenCount(), "Should be exactly one token");

        // no username match
        result = mockMvc.perform(post("/api/passwordReset/initiateResetForUser/randomnamenotinldap"));
        result.andExpect(status().isNotFound());
        Assertions.assertEquals(1, tokenRecordStorage.getTokenCount(), "Should be exactly one token");

        // no email for user
        result = mockMvc.perform(post("/api/passwordReset/initiateResetForUser/jsmith"));
        result.andExpect(status().isNotFound());
        Assertions.assertEquals(1, tokenRecordStorage.getTokenCount(), "Should be exactly one token");

        // re-request for banderson and ensure only a single token exists
        result =  mockMvc.perform(post("/api/passwordReset/initiateResetForUser/banderson"));
        result.andExpect(status().isOk());
        Assertions.assertEquals(1, tokenRecordStorage.getTokenCount(), "Should be exactly one token");

        // email failed for whatever reason
        Mockito.when(emailService.send(eq(emailProperties), eq("banderson@mail.com"), Mockito.anyString())).thenReturn(false);
        result =  mockMvc.perform(post("/api/passwordReset/initiateResetForUser/banderson"));
        result.andExpect(status().is5xxServerError());
        Assertions.assertEquals(1, tokenRecordStorage.getTokenCount(), "Should be exactly one token");
    }

//    @Test
//    public void test_stuff() {
//
//    }

    class TestTRS extends TokenRecordStorage.IN_MEMORY {
        public void clear() { map = new HashMap<>(); }
        public int getTokenCount() { return map.keySet().size(); }
    }
}