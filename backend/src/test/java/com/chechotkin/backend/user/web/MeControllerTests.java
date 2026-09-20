package com.chechotkin.backend.user.web;

import com.chechotkin.backend.security.AuthPrincipal;
import com.chechotkin.backend.security.SecurityConfig;
import com.chechotkin.backend.user.UserRepoFake;
import com.chechotkin.backend.user.controller.MeController;
import com.chechotkin.backend.user.model.User;
import com.chechotkin.backend.user.service.UserServiceImpl;
import com.chechotkin.backend.user.usecase.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(MeController.class)
@Import(SecurityConfig.class)
public class MeControllerTests {
    @Autowired
    MockMvc mockMvc;

    @TestConfiguration
    static class FakeBeans{
        @Bean
        UserRepoFake userRepo() {
            return new UserRepoFake();
        }
        @Bean
        UserService userService( UserRepoFake userRepo){
            return  new UserServiceImpl(userRepo, Clock.systemUTC());
        }
    }


    private static final String EMAIL = "max123@gmail.com";

    @Autowired
    private UserRepoFake userRepo;

    @Test
    void anonymousRequest_isRejectedWithoutBasicChallenge() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }
    @Test
    void authenticatedRequest_returnsTheCallersProfile() throws Exception{
        User user = userRepo.upsertByEmail(EMAIL, Instant.parse("2026-01-01T00:00:00Z"));
        AuthPrincipal authPrincipal = new AuthPrincipal(user.id(), user.email());

        mockMvc.perform(get("/api/me")
                        .with(authentication(UsernamePasswordAuthenticationToken.authenticated(
                                authPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.id").value(user.id().intValue()))
                .andExpect(jsonPath("$.displayedName").value(EMAIL));


    }
    @Test
    void principalWithoutMatchingUser_isRejected() throws Exception{
        Long fakeId = 123L;
        AuthPrincipal authPrincipal = new AuthPrincipal(fakeId, EMAIL);
        mockMvc.perform(get("/api/me")
                .with(authentication(UsernamePasswordAuthenticationToken.authenticated(
                        authPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isUnauthorized());

    }

}
