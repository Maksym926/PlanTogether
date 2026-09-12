package com.chechotkin.backend.auth.web.config;

import com.chechotkin.backend.auth.helpers.CodeGenerator;
import com.chechotkin.backend.auth.helpers.CodeGeneratorImpl;
import com.chechotkin.backend.auth.repo.LoginTokenRepo;
import com.chechotkin.backend.auth.service.AuthServiceImpl;
import com.chechotkin.backend.auth.service.LoginTokenServiceServiceImpl;
import com.chechotkin.backend.auth.usecase.AuthService;
import com.chechotkin.backend.auth.usecase.LoginTokenService;
import com.chechotkin.backend.user.repo.UserRepo;
import com.chechotkin.backend.user.service.UserServiceImpl;
import com.chechotkin.backend.user.usecase.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AuthConfig {
    @Bean
    public AuthService authService(LoginTokenService loginTokenService, UserService userService){
        return new AuthServiceImpl(loginTokenService, userService);
    }
    @Bean
    public LoginTokenService loginTokenService(CodeGenerator generator, LoginTokenRepo tokenRepo, Clock clock){
        return new LoginTokenServiceServiceImpl(generator, tokenRepo, clock);
    }

    @Bean
    public UserService userService(UserRepo userRepo, Clock clock){
        return  new UserServiceImpl(userRepo, clock);
    }

    @Bean
    public CodeGenerator codeGenerator(){
        return  new CodeGeneratorImpl();
    }
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
