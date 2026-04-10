package com.ohgiraffers.team3backendkms.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MentoringSchemaVerificationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("멘토링 스키마 검증")
    void verifyMentoringSchema() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(hasTable(connection, "mentoring_request"));
            assertTrue(hasTable(connection, "mentoring"));
            assertTrue(hasTable(connection, "employee_mentoring_field"));

            assertTrue(hasColumn(connection, "mentoring_request", "rejected_mentor_ids"));
            assertTrue(hasUniqueIndex(connection, "mentoring", "UQ_MENTORING_REQUEST"));
        }
    }

    private boolean hasTable(Connection connection, String tableName) throws Exception {
        try (ResultSet rs = connection.getMetaData()
                .getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private boolean hasColumn(Connection connection, String tableName, String columnName) throws Exception {
        try (ResultSet rs = connection.getMetaData()
                .getColumns(connection.getCatalog(), null, tableName, columnName)) {
            return rs.next();
        }
    }

    private boolean hasUniqueIndex(Connection connection, String tableName, String indexName) throws Exception {
        try (ResultSet rs = connection.getMetaData()
                .getIndexInfo(connection.getCatalog(), null, tableName, true, false)) {
            while (rs.next()) {
                if (indexName.equalsIgnoreCase(rs.getString("INDEX_NAME"))) {
                    return true;
                }
            }
            return false;
        }
    }
}
