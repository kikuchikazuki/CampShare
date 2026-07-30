package com.example.campshare.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StaticResourceSecurityTest.NoopController.class)
@Import(SecurityConfig.class)
class StaticResourceSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousRequestCanLoadSiteStylesheet() throws Exception {
        mockMvc.perform(get("/css/site.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("text/css")));
    }

    @Controller
    static class NoopController {
    }
}
