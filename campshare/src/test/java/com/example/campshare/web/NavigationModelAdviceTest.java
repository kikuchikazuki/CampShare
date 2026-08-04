package com.example.campshare.web;

import com.example.campshare.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.ObjectProvider;

class NavigationModelAdviceTest {

    private final com.example.campshare.user.UserRepository userRepository = mock(com.example.campshare.user.UserRepository.class);
    private final ObjectProvider<com.example.campshare.user.UserRepository> userRepositories = mock(ObjectProvider.class);
    private final NavigationModelAdvice advice = new NavigationModelAdvice(userRepositories);

    NavigationModelAdviceTest() {
        when(userRepositories.getIfAvailable()).thenReturn(userRepository);
    }

    @Test
    void anonymousNavigationHasNoAccountActions() {
        ConcurrentModel model = new ConcurrentModel();

        advice.addNavigationState(null, model);

        assertFalse((boolean) model.getAttribute("navAuthenticated"));
        assertNull(model.getAttribute("navEmail"));
        assertNull(model.getAttribute("navDisplayName"));
        assertFalse((boolean) model.getAttribute("navAdmin"));
    }

    @Test
    void userNavigationShowsIdentityWithoutAdminAction() {
        ConcurrentModel model = new ConcurrentModel();
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("camper@example.com", "password", "ROLE_USER");
        authentication.setAuthenticated(true);
        User user = new User("キャンパー", "camper@example.com", "hash", null);
        when(userRepository.findByEmail("camper@example.com")).thenReturn(java.util.Optional.of(user));

        advice.addNavigationState(authentication, model);

        assertTrue((boolean) model.getAttribute("navAuthenticated"));
        assertEquals("camper@example.com", model.getAttribute("navEmail"));
        assertEquals("キャンパー", model.getAttribute("navDisplayName"));
        assertFalse((boolean) model.getAttribute("navAdmin"));
    }

    @Test
    void adminNavigationIncludesAdminAction() {
        ConcurrentModel model = new ConcurrentModel();
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("admin@example.com", "password", "ROLE_ADMIN");
        authentication.setAuthenticated(true);
        User user = new User("管理者", "admin@example.com", "hash", null);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(java.util.Optional.of(user));

        advice.addNavigationState(authentication, model);

        assertTrue((boolean) model.getAttribute("navAuthenticated"));
        assertEquals("admin@example.com", model.getAttribute("navEmail"));
        assertEquals("管理者", model.getAttribute("navDisplayName"));
        assertTrue((boolean) model.getAttribute("navAdmin"));
    }
}
