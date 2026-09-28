package com.chechotkin.backend.auth;

import com.chechotkin.backend.AbstractIT;
import com.chechotkin.backend.auth.helpers.CodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The whole login path against a real Postgres: request a code, verify it,
 * and use the resulting session on /api/me.
 */
@AutoConfigureMockMvc
class AuthFlowIT extends AbstractIT {

    private static final String EMAIL = "max@gmail.com";
    private static final String CODE = "123456";

    /**
     * The real generator is random and only the hash is stored, so a test
     * could never learn the code it is supposed to submit.
     */
    @TestConfiguration
    static class FixedCode {
        @Bean
        @Primary
        CodeGenerator fixedCodeGenerator() {
            return () -> CODE;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbc;

    @BeforeEach
    void clearTables() {
        jdbc.sql("TRUNCATE users, login_token RESTART IDENTITY").update();
    }

    private MockHttpSession requestCode() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/request").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s"}""".formatted(EMAIL)))
                .andExpect(status().isAccepted())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private MvcResult verify(MockHttpSession session, String code) throws Exception {
        return mockMvc.perform(post("/api/auth/verify").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "code": "%s"}""".formatted(EMAIL, code)))
                .andReturn();
    }

    @Test
    void verifiedCode_logsTheUserInAndMeReturnsTheProfile() throws Exception {
        MockHttpSession session = requestCode();

        MvcResult login = verify(session, CODE);
        assertThat(login.getResponse().getStatus()).isEqualTo(200);

        mockMvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void theLoginSurvivesASecondRequest() throws Exception {
        MockHttpSession session = requestCode();
        verify(session, CODE);

        mockMvc.perform(get("/api/me").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/api/me").session(session)).andExpect(status().isOk());
    }

    @Test
    void meWithoutASession_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verify_rotatesTheSessionId() throws Exception {
        MockHttpSession session = requestCode();
        String idBeforeLogin = session.getId();

        verify(session, CODE);

        assertThat(session.getId()).isNotEqualTo(idBeforeLogin);
    }

    @Test
    void wrongCode_leavesTheCallerAnonymous() throws Exception {
        MockHttpSession session = requestCode();

        verify(session, "000000");

        mockMvc.perform(get("/api/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verifiedCode_createsExactlyOneUser() throws Exception {
        MockHttpSession session = requestCode();
        verify(session, CODE);

        long users = jdbc.sql("SELECT count(*) FROM users").query(Long.class).single();
        assertThat(users).isEqualTo(1);
    }
}
