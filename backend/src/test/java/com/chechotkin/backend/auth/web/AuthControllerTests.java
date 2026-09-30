package com.chechotkin.backend.auth.web;

import com.chechotkin.backend.auth.FixedCodeGenerator;
import com.chechotkin.backend.auth.LoginTokenRepoFake;
import com.chechotkin.backend.auth.notifier.CodeNotifierFake;
import com.chechotkin.backend.auth.repo.LoginTokenRepo;
import com.chechotkin.backend.auth.usecase.CodeNotifier;
import com.chechotkin.backend.auth.web.config.AuthConfig;
import com.chechotkin.backend.auth.web.controller.AuthController;
import com.chechotkin.backend.auth.web.dto.RequestToken;
import com.chechotkin.backend.auth.web.dto.VerifyToken;
import com.chechotkin.backend.security.SecurityConfig;
import com.chechotkin.backend.user.UserRepoFake;
import com.chechotkin.backend.user.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({AuthConfig.class, SecurityConfig.class})
public class AuthControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class FakeBeans {
        @Bean
        @Primary
        LoginTokenRepo loginTokenRepo() {
            return new LoginTokenRepoFake();
        }

        @Bean
        @Primary
        UserRepo userRepo() {
            return new UserRepoFake();
        }

        @Bean
        @Primary
        FixedCodeGenerator fixedCodeGenerator() {
            return new FixedCodeGenerator("123456");
        }

        @Bean
        @Primary
        CodeNotifierFake fakeCodeNotifier() {return  new CodeNotifierFake();}


    }



    @Autowired
    FixedCodeGenerator codeGenerator;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private LoginTokenRepo loginTokenRepoFake;

    @Autowired
    private CodeNotifierFake notifier;

    private final String EMAIL = "max123@gmail.com";

    @BeforeEach
    void resetNotifier() {
        notifier.clear();
    }

    @Test
    void aFailedDelivery_stillReturns202() throws Exception {
        notifier.failWith(new RuntimeException("smtp down"));

        mockMvc.perform(post("/api/auth/request").session(new MockHttpSession()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequestToken(EMAIL))))
                .andExpect(status().isAccepted());
    }

    @Test
    void requestForTestEventShouldReturn202Test() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/auth/request").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequestToken(EMAIL))))
                .andExpect(status().isAccepted());

    }
    @Test
    void requestTwiceForSameEmail_invalidatesTheFirstCode() throws Exception {
        MockHttpSession session = new MockHttpSession();

        String firstCode = "654321";
        String secondCode = "999999";

        codeGenerator.setCode(firstCode);
        mockMvc.perform(post("/api/auth/request").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequestToken(EMAIL))))
                .andExpect(status().isAccepted());

        codeGenerator.setCode(secondCode);
        mockMvc.perform(post("/api/auth/request").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequestToken(EMAIL))))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/auth/verify").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyToken(EMAIL, firstCode))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("wrong_code"));

        mockMvc.perform(post("/api/auth/verify").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyToken(EMAIL, secondCode))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void invalidEmail_isReportedPerField() throws Exception {
        mockMvc.perform(post("/api/auth/request").session(new MockHttpSession()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequestToken("not-an-email"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_failed"))
                .andExpect(jsonPath("$.fieldErrors.email").isArray())
                .andExpect(jsonPath("$.fieldErrors.email[0]").exists());
    }

    @Test
    void malformedJson_isRejectedInTheSameShape() throws Exception {
        mockMvc.perform(post("/api/auth/request").session(new MockHttpSession()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("malformed_request"));
    }

    @Test
    void exhaustedAttempts_areReportedByCode() throws Exception {
        String email = "attempts@gmail.com";
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/api/auth/request").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequestToken(email))))
                .andExpect(status().isAccepted());

        String wrongCode = objectMapper.writeValueAsString(new VerifyToken(email, "000000"));
        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/api/auth/verify").session(session).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(wrongCode))
                    .andExpect(status().isBadRequest());
        }

        mockMvc.perform(post("/api/auth/verify").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongCode))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("too_many_attempts"));
    }
}
