package com.chechotkin.backend.healthCheck;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(HealthCheckController.class)
public class HealthCheckTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void checkTheCommunicationAcrossServer() throws Exception {
        mockMvc.perform(get("/api/health-check"))
                .andExpect(status().isOk())
                .andExpect(content().string("Up"));

    }
}
