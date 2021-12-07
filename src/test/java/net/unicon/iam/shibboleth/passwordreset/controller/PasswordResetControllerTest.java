package net.unicon.iam.shibboleth.passwordreset.controller;

import net.unicon.iam.shibboleth.passwordreset.AbstractEmbeddedLdapTest;
import net.unicon.iam.shibboleth.passwordreset.service.LdapPasswordManagementService;
import net.unicon.iam.shibboleth.passwordreset.support.ldap.LdapProperties;
import net.unicon.iam.shibboleth.passwordreset.token.InMemoryTokenRecordStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ldaptive.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PasswordResetControllerTest extends AbstractEmbeddedLdapTest {
    @Autowired
    private TestingLdapPasswordManagementService passwordManagementService;

    private MockMvc mockMvc;
    private TestingTRS tokenRecordStorage = new TestingTRS();

    @BeforeEach
    public void beforeEach() {
        tokenRecordStorage.clear();
    }

    @BeforeAll
    public void setup() {
        PasswordResetController controller = new PasswordResetController();
        passwordManagementService.setTokenRecordStorage(tokenRecordStorage);
        controller.passwordManagementService = passwordManagementService;
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void test_initiateResetForUser() throws Exception {
        // easy path
        var result =  mockMvc.perform(post("/api/passwordReset/initiateResetForUser/banderson"));
        result.andExpect(status().isOk());
        Assertions.assertEquals(1, tokenRecordStorage.getTokenCount(), "Should be exactly one token");
        String token = result.andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(token.equals(tokenRecordStorage.getSingleToken()));

        // no username match
        tokenRecordStorage.clear();
        result = mockMvc.perform(post("/api/passwordReset/initiateResetForUser/randomnamenotinldap"));
        result.andExpect(status().isNotFound());
        Assertions.assertEquals(0, tokenRecordStorage.getTokenCount(), "Should be no token");

        // re-request for banderson and ensure only a single token exists
        result =  mockMvc.perform(post("/api/passwordReset/initiateResetForUser/banderson"));
        result.andExpect(status().isOk());
        Assertions.assertEquals(1, tokenRecordStorage.getTokenCount(), "Should be exactly one token");
        result =  mockMvc.perform(post("/api/passwordReset/initiateResetForUser/banderson"));
        result.andExpect(status().isOk());
        Assertions.assertEquals(1, tokenRecordStorage.getTokenCount(), "Should be exactly one token");
    }

    @Test
    public void test_resetPassword() throws Exception {
        // random token when there are none - 404 NOT_FOUND if the token does not produce a username
        String jsonBody = "{ \"password\" : \"newpassword2\", \"confirmedPassword\" : \"newpassword2\" }";
        var result = mockMvc.perform(patch("/api/passwordReset/randomvalue").contentType(MediaType.APPLICATION_JSON).content(jsonBody));
        result.andExpect(status().isNotFound());
        Assertions.assertEquals(0, tokenRecordStorage.getTokenCount(), "Should be exactly no tokens");

        // Standard good path
        mockMvc.perform(post("/api/passwordReset/initiateResetForUser/banderson"));
        Assertions.assertEquals(1, tokenRecordStorage.getTokenCount(), "Should be exactly one token");
        Assertions.assertEquals("password", passwordManagementService.findUserPassword("banderson"), "initial password not correct");

        String token = tokenRecordStorage.getSingleToken();
        result = mockMvc.perform(patch("/api/passwordReset/"+token).contentType(MediaType.APPLICATION_JSON).content(jsonBody));
        result.andExpect(status().isNoContent());
        Assertions.assertEquals(0, tokenRecordStorage.getTokenCount(), "Should be exactly no tokens");
        Assertions.assertEquals("newpassword2", passwordManagementService.findUserPassword("banderson"), "updated password not correct");

        // 400 BAD_REQUEST if the password or passwordConfirm values supplied are empty(null) or do not match
        mockMvc.perform(post("/api/passwordReset/initiateResetForUser/banderson"));
        Assertions.assertEquals(1, tokenRecordStorage.getTokenCount(), "Should be exactly one token");
        Assertions.assertEquals("newpassword2", passwordManagementService.findUserPassword("banderson"), "initial password not correct");

        token = tokenRecordStorage.getSingleToken();
        jsonBody = "{ \"password\" : \"newpassword2\", \"confirmedPassword\" : \"notamatch\" }";
        result = mockMvc.perform(patch("/api/passwordReset/"+token).contentType(MediaType.APPLICATION_JSON).content(jsonBody));
        result.andExpect(status().isBadRequest());
        Assertions.assertEquals(0, tokenRecordStorage.getTokenCount(), "Should be exactly no tokens");
        Assertions.assertEquals("newpassword2", passwordManagementService.findUserPassword("banderson"), "password not correct");

        jsonBody = "{ \"password\" : \"\"";
        result = mockMvc.perform(patch("/api/passwordReset/"+token).contentType(MediaType.APPLICATION_JSON).content(jsonBody));
        result.andExpect(status().isBadRequest());
        Assertions.assertEquals(0, tokenRecordStorage.getTokenCount(), "Should be exactly no tokens");
        Assertions.assertEquals("newpassword2", passwordManagementService.findUserPassword("banderson"), "password not correct");
    }

    /**
     * Gives access to the in memory map to validate that the correct things are happening
     */
    static class TestingTRS extends InMemoryTokenRecordStorage {
        public void clear() { map = new HashMap<>(); }
        public int getTokenCount() { return map.keySet().size(); }
        public String getSingleToken() { return map.keySet().toArray()[0].toString(); }
    }

    static class TestingLdapPasswordManagementService extends LdapPasswordManagementService {
        public TestingLdapPasswordManagementService(LdapProperties ldapProperties) {
            super(ldapProperties);
        }

        public String findUserPassword(String username) {
            Optional<SearchResponse> result = findSearchResultFor(username, "userPassword");
            return result.get().getEntry().getAttribute("userPassword").getStringValue();
        }

    }
}