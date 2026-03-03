package com.hi.insurance_agent.db;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Objects;

public class SHASearch {
    static String SHASQL = "SELECT metadata->>'sha' AS sha  FROM insurance_vector_store";

    public static List<String> getAllSha(JdbcTemplate jdbc) {
        return jdbc.queryForList(SHASQL).stream()
                .map(row -> (String) row.get("sha"))
                .filter(Objects::nonNull)
                .toList();
    }

    public static boolean existsBySha(JdbcTemplate jdbc, String sha256) {
        String sql = "SELECT EXISTS (SELECT 1 FROM insurance_vector_store WHERE metadata->>'sha' = ?)";
        Boolean exists = jdbc.queryForObject(sql, Boolean.class, sha256);
        return exists != null && exists;
    }
}
