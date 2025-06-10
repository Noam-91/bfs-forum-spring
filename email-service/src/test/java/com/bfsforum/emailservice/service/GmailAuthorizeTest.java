package com.bfsforum.emailservice.service;

import com.google.api.client.auth.oauth2.Credential;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;

class GmailAuthorizeTest {

    @Test
    void testGetCredentials_shouldReturnCredential() {
        try {
            Credential credential = GmailAuthorize.getCredentials();

            assertNotNull(credential, "Credential should not be null");
            assertNotNull(credential.getAccessToken(), "Access token should not be null or empty");

        } catch (IOException | GeneralSecurityException e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
}