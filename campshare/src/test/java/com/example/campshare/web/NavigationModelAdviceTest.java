package com.example.campshare.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NavigationModelAdviceTest {

    private final NavigationModelAdvice advice = new NavigationModelAdvice();

    @Test
    void anonymousNavigationHasNoAccountActions() {
        ConcurrentModel model = new ConcurrentModel();

        advice.addNavigationState(null, model);

        assertFalse((boolean) model.getAttribute("navAuthenticated"));
        assertNull(model.getAttribute("navEmail"));
        assertFalse((boolean) model.getAttribute("navAdmin"));
    }

    @Test
    void userNavigationShowsIdentityWithoutAdminAction() {
        ConcurrentModel model = new ConcurrentModel();
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("camper@example.com", "password", "ROLE_USER");
        authentication.setAuthenticated(true);

        advice.addNavigationState(authentication, model);

        assertTrue((boolean) model.getAttribute("navAuthenticated"));
        assertEquals("camper@example.com", model.getAttribute("navEmail"));
        assertFalse((boolean) model.getAttribute("navAdmin"));
    }

    @Test
    void adminNavigationIncludesAdminAction() {
        ConcurrentModel model = new ConcurrentModel();
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("admin@example.com", "password", "ROLE_ADMIN");
        authentication.setAuthenticated(true);

        advice.addNavigationState(authentication, model);

        assertTrue((boolean) model.getAttribute("navAuthenticated"));
        assertEquals("admin@example.com", model.getAttribute("navEmail"));
        assertTrue((boolean) model.getAttribute("navAdmin"));
    }
}
