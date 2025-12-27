package com.example.cloudstack.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSchemaFixes implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureAvatarColumn();
    }

    private void ensureAvatarColumn() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() " +
                            "AND TABLE_NAME = 'users' " +
                            "AND COLUMN_NAME = 'avatar'");
            if (rows.isEmpty()) {
                log.warn("users.avatar column not found");
                return;
            }

            String dataType = String.valueOf(rows.get(0).get("DATA_TYPE")).toLowerCase(Locale.ROOT);
            if ("mediumtext".equals(dataType) || "longtext".equals(dataType)) {
                return;
            }

            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN avatar MEDIUMTEXT");
            log.info("Updated users.avatar column to MEDIUMTEXT");
        } catch (Exception e) {
            log.warn("Failed to ensure users.avatar column size", e);
        }
    }
}
