package com.ohgiraffers.team3backendkms.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MentoringSchemaMigrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("멘토링 스키마 마이그레이션")
    void migrateMentoringSchema() throws Exception {
        assertNotNull(dataSource);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            assertEquals("setodb", connection.getCatalog());

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `employee_mentoring_field` (
                        `employee_mentoring_field_id` BIGINT NOT NULL,
                        `employee_id` BIGINT NOT NULL,
                        `mentoring_field` VARCHAR(100) NOT NULL,
                        `created_at` TIMESTAMP NULL,
                        `created_by` BIGINT NULL,
                        `updated_at` TIMESTAMP NULL,
                        `updated_by` BIGINT NULL,
                        PRIMARY KEY (`employee_mentoring_field_id`)
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `mentoring_request` (
                        `request_id` BIGINT NOT NULL,
                        `mentee_id` BIGINT NOT NULL,
                        `mentor_id` BIGINT NULL,
                        `article_id` BIGINT NULL,
                        `mentoring_field` VARCHAR(100) NOT NULL,
                        `request_title` VARCHAR(255) NOT NULL,
                        `request_content` VARCHAR(1000) NOT NULL,
                        `mentoring_duration_weeks` INT NULL,
                        `mentoring_frequency` VARCHAR(50) NULL,
                        `request_priority` ENUM('HIGH', 'MEDIUM', 'LOW') NULL,
                        `request_status` ENUM('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL,
                        `reject_reason` VARCHAR(500) NULL,
                        `rejected_mentor_ids` JSON NULL,
                        `requested_at` TIMESTAMP NULL,
                        `processed_at` TIMESTAMP NULL,
                        `created_at` TIMESTAMP NULL,
                        `created_by` BIGINT NULL,
                        `updated_at` TIMESTAMP NULL,
                        `updated_by` BIGINT NULL,
                        PRIMARY KEY (`request_id`)
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `mentoring` (
                        `mentoring_id` BIGINT NOT NULL,
                        `request_id` BIGINT NOT NULL,
                        `mentor_id` BIGINT NOT NULL,
                        `mentee_id` BIGINT NOT NULL,
                        `mentoring_status` ENUM('IN_PROGRESS', 'COMPLETED') NOT NULL,
                        `started_at` TIMESTAMP NULL,
                        `completed_at` TIMESTAMP NULL,
                        `created_at` TIMESTAMP NULL,
                        `created_by` BIGINT NULL,
                        `updated_at` TIMESTAMP NULL,
                        `updated_by` BIGINT NULL,
                        PRIMARY KEY (`mentoring_id`)
                    )
                    """);

            addColumnIfMissing(statement, connection, "employee_mentoring_field", "employee_id",
                    "ALTER TABLE `employee_mentoring_field` ADD COLUMN `employee_id` BIGINT NOT NULL");
            addColumnIfMissing(statement, connection, "employee_mentoring_field", "mentoring_field",
                    "ALTER TABLE `employee_mentoring_field` ADD COLUMN `mentoring_field` VARCHAR(100) NOT NULL");

            addColumnIfMissing(statement, connection, "mentoring_request", "mentee_id",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `mentee_id` BIGINT NOT NULL");
            addColumnIfMissing(statement, connection, "mentoring_request", "mentor_id",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `mentor_id` BIGINT NULL");
            addColumnIfMissing(statement, connection, "mentoring_request", "article_id",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `article_id` BIGINT NULL");
            addColumnIfMissing(statement, connection, "mentoring_request", "mentoring_field",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `mentoring_field` VARCHAR(100) NOT NULL");
            addColumnIfMissing(statement, connection, "mentoring_request", "request_title",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `request_title` VARCHAR(255) NOT NULL");
            addColumnIfMissing(statement, connection, "mentoring_request", "request_content",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `request_content` VARCHAR(1000) NOT NULL");
            addColumnIfMissing(statement, connection, "mentoring_request", "mentoring_duration_weeks",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `mentoring_duration_weeks` INT NULL");
            addColumnIfMissing(statement, connection, "mentoring_request", "mentoring_frequency",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `mentoring_frequency` VARCHAR(50) NULL");
            addColumnIfMissing(statement, connection, "mentoring_request", "request_priority",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `request_priority` ENUM('HIGH', 'MEDIUM', 'LOW') NULL");
            addColumnIfMissing(statement, connection, "mentoring_request", "request_status",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `request_status` ENUM('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL DEFAULT 'PENDING'");
            addColumnIfMissing(statement, connection, "mentoring_request", "reject_reason",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `reject_reason` VARCHAR(500) NULL");
            addColumnIfMissing(statement, connection, "mentoring_request", "rejected_mentor_ids",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `rejected_mentor_ids` JSON NULL");
            addColumnIfMissing(statement, connection, "mentoring_request", "requested_at",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `requested_at` TIMESTAMP NULL");
            addColumnIfMissing(statement, connection, "mentoring_request", "processed_at",
                    "ALTER TABLE `mentoring_request` ADD COLUMN `processed_at` TIMESTAMP NULL");

            addColumnIfMissing(statement, connection, "mentoring", "request_id",
                    "ALTER TABLE `mentoring` ADD COLUMN `request_id` BIGINT NOT NULL");
            addColumnIfMissing(statement, connection, "mentoring", "mentor_id",
                    "ALTER TABLE `mentoring` ADD COLUMN `mentor_id` BIGINT NOT NULL");
            addColumnIfMissing(statement, connection, "mentoring", "mentee_id",
                    "ALTER TABLE `mentoring` ADD COLUMN `mentee_id` BIGINT NOT NULL");
            addColumnIfMissing(statement, connection, "mentoring", "mentoring_status",
                    "ALTER TABLE `mentoring` ADD COLUMN `mentoring_status` ENUM('IN_PROGRESS', 'COMPLETED') NOT NULL DEFAULT 'IN_PROGRESS'");
            addColumnIfMissing(statement, connection, "mentoring", "started_at",
                    "ALTER TABLE `mentoring` ADD COLUMN `started_at` TIMESTAMP NULL");
            addColumnIfMissing(statement, connection, "mentoring", "completed_at",
                    "ALTER TABLE `mentoring` ADD COLUMN `completed_at` TIMESTAMP NULL");

            addUniqueIndexIfMissing(statement, connection, "mentoring", "UQ_MENTORING_REQUEST",
                    "ALTER TABLE `mentoring` ADD CONSTRAINT `UQ_MENTORING_REQUEST` UNIQUE (`request_id`)");
            addUniqueIndexIfMissing(statement, connection, "employee_mentoring_field", "UQ_EMPLOYEE_MENTORING_FIELD",
                    "ALTER TABLE `employee_mentoring_field` ADD CONSTRAINT `UQ_EMPLOYEE_MENTORING_FIELD` UNIQUE (`employee_id`, `mentoring_field`)");

            addForeignKeyIfMissing(statement, connection, "mentoring_request", "FK_employee_TO_mentoring_request_1",
                    """
                            ALTER TABLE `mentoring_request`
                            ADD CONSTRAINT `FK_employee_TO_mentoring_request_1`
                            FOREIGN KEY (`mentee_id`) REFERENCES `employee` (`employee_id`)
                            """);
            addForeignKeyIfMissing(statement, connection, "mentoring_request", "FK_employee_TO_mentoring_request_2",
                    """
                            ALTER TABLE `mentoring_request`
                            ADD CONSTRAINT `FK_employee_TO_mentoring_request_2`
                            FOREIGN KEY (`mentor_id`) REFERENCES `employee` (`employee_id`)
                            """);
            addForeignKeyIfMissing(statement, connection, "mentoring_request", "FK_knowledge_article_TO_mentoring_request_1",
                    """
                            ALTER TABLE `mentoring_request`
                            ADD CONSTRAINT `FK_knowledge_article_TO_mentoring_request_1`
                            FOREIGN KEY (`article_id`) REFERENCES `knowledge_article` (`article_id`)
                            """);
            addForeignKeyIfMissing(statement, connection, "mentoring", "FK_mentoring_request_TO_mentoring_1",
                    """
                            ALTER TABLE `mentoring`
                            ADD CONSTRAINT `FK_mentoring_request_TO_mentoring_1`
                            FOREIGN KEY (`request_id`) REFERENCES `mentoring_request` (`request_id`)
                            """);
            addForeignKeyIfMissing(statement, connection, "mentoring", "FK_employee_TO_mentoring_1",
                    """
                            ALTER TABLE `mentoring`
                            ADD CONSTRAINT `FK_employee_TO_mentoring_1`
                            FOREIGN KEY (`mentor_id`) REFERENCES `employee` (`employee_id`)
                            """);
            addForeignKeyIfMissing(statement, connection, "mentoring", "FK_employee_TO_mentoring_2",
                    """
                            ALTER TABLE `mentoring`
                            ADD CONSTRAINT `FK_employee_TO_mentoring_2`
                            FOREIGN KEY (`mentee_id`) REFERENCES `employee` (`employee_id`)
                            """);
            addForeignKeyIfMissing(statement, connection, "employee_mentoring_field", "FK_employee_TO_employee_mentoring_field_1",
                    """
                            ALTER TABLE `employee_mentoring_field`
                            ADD CONSTRAINT `FK_employee_TO_employee_mentoring_field_1`
                            FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`)
                            """);
        }
    }

    private void addColumnIfMissing(Statement statement, Connection connection, String tableName,
                                    String columnName, String ddl) throws Exception {
        try (ResultSet rs = connection.getMetaData()
                .getColumns(connection.getCatalog(), null, tableName, columnName)) {
            if (!rs.next()) {
                statement.executeUpdate(ddl);
            }
        }
    }

    private void addUniqueIndexIfMissing(Statement statement, Connection connection, String tableName,
                                         String indexName, String ddl) throws Exception {
        try (ResultSet rs = connection.getMetaData()
                .getIndexInfo(connection.getCatalog(), null, tableName, true, false)) {
            while (rs.next()) {
                if (indexName.equalsIgnoreCase(rs.getString("INDEX_NAME"))) {
                    return;
                }
            }
        }
        statement.executeUpdate(ddl);
    }

    private void addForeignKeyIfMissing(Statement statement, Connection connection, String tableName,
                                        String fkName, String ddl) throws Exception {
        try (ResultSet rs = connection.getMetaData()
                .getImportedKeys(connection.getCatalog(), null, tableName)) {
            while (rs.next()) {
                if (fkName.equalsIgnoreCase(rs.getString("FK_NAME"))) {
                    return;
                }
            }
        }
        statement.executeUpdate(ddl);
    }
}
