package com.chechotkin.backend.auth.web.config;

import com.chechotkin.backend.auth.service.CodeNotifierImpl;
import com.chechotkin.backend.auth.service.LoginTokenServiceServiceImpl;
import com.chechotkin.backend.auth.usecase.CodeNotifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;


@Configuration
public class MailConfig {

    @Bean
    public CodeNotifier codeNotifier(JavaMailSender mailSender,
                                     @Value("${app.mail.from}") String from) {

        return new CodeNotifierImpl(mailSender, from, LoginTokenServiceServiceImpl.TIME_TO_LIVE.toMinutes());
    }
}
