package com.example.campshare.web;

import com.example.campshare.user.DuplicateEmailException;
import com.example.campshare.user.SignupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(SignupController.class)
class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SignupService signupService;

    @Test
    void signupPageReturnsSignupView() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("signupForm"));
    }

    @Test
    void signupRejectsBlankFields() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("displayName", "")
                        .param("email", "")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeHasFieldErrors("signupForm", "displayName", "email", "password"));
    }

    @Test
    void signupRedirectsAfterSuccess() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("displayName", "Camper Taro")
                        .param("email", "taro@example.com")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup?registered=true"));
    }

    @Test
    void signupShowsEmailErrorWhenEmailIsDuplicate() throws Exception {
        doThrow(new DuplicateEmailException()).when(signupService).register(any());

        mockMvc.perform(post("/signup")
                        .param("displayName", "Camper Taro")
                        .param("email", "taro@example.com")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeHasFieldErrors("signupForm", "email"));
    }
}
