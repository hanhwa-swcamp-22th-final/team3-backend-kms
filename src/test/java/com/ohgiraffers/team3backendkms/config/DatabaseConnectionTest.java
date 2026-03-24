package com.ohgiraffers.team3backendkms.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("DB 연결 테스트")
    void testDatabaseConnection() throws Exception {
        assertNotNull(dataSource, "DataSource should not be null");

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");

            String catalog = connection.getCatalog();
            System.out.println("Connected to database: " + catalog);
            System.out.println("DB URL: " + connection.getMetaData().getURL());
            System.out.println("DB User: " + connection.getMetaData().getUserName());
            System.out.println("DB Product: " + connection.getMetaData().getDatabaseProductName()
                    + " " + connection.getMetaData().getDatabaseProductVersion());

            assertEquals("setodb", catalog, "Should be connected to setodb schema");
        }
    }
}
