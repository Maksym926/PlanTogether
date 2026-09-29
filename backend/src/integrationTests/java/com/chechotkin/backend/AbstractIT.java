package com.chechotkin.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.TimeZone;


@SpringBootTest
public abstract class AbstractIT {

    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        postgres.start();
    }
}
