package com.chechotkin.backend.auth.web;

import com.chechotkin.backend.auth.web.config.AuthConfig;
import com.chechotkin.backend.auth.web.controller.AuthController;
import com.chechotkin.backend.auth.web.controller.CsrfController;
import com.chechotkin.backend.auth.web.dto.RequestToken;
import com.chechotkin.backend.security.SecurityConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest({AuthController.class, CsrfController.class})
@Import({AuthConfig.class, SecurityConfig.class, AuthControllerTests.FakeBeans.class})
class CsrfCookieTests {

    private static final String EMAIL = "max@gmail.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Cookie fetchCsrfCookie() throws Exception {
        Cookie cookie = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isNoContent())
                .andReturn()
                .getResponse()
                .getCookie("XSRF-TOKEN");

        assertThat(cookie).isNotNull();
        return cookie;
    }

    @Test
    void csrfEndpoint_setsACookieJavaScriptCanRead() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }

    @Test
    void postWithTheTokenEchoedInAHeader_isAccepted() throws Exception {
        Cookie csrf = fetchCsrfCookie();

        mockMvc.perform(post("/api/auth/request")
                        .cookie(csrf)
                        .header("X-XSRF-TOKEN", csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequestToken(EMAIL))))
                .andExpect(status().isAccepted());
    }

    @Test
    void postWithTheCookieButNoHeader_isForbidden() throws Exception {

        Cookie csrf = fetchCsrfCookie();

        mockMvc.perform(post("/api/auth/request")
                        .cookie(csrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequestToken(EMAIL))))
                .andExpect(status().isForbidden());
    }
}
