package com.example.locationsystem.userAccess;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class UserAccessDao {

    private final JdbcTemplate jdbcTemplate;

    public UserAccessDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Async
    public CompletableFuture<Void> saveUserAccess(UserAccess userAccess) {
        return CompletableFuture.runAsync(() -> {
            jdbcTemplate.update("INSERT INTO accesses(title,location_id,user_id) VALUES(?,?,?)",
                    userAccess.getTitle(), userAccess.getLocation().getId(), userAccess.getUser().getId());
        });
    }

    @Async
    public CompletableFuture<Void> changeUserAccess(Long locationId, Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UserAccess userAccess = jdbcTemplate.queryForObject("SELECT * FROM accesses WHERE location_id = ? AND user_id = ?",
                        BeanPropertyRowMapper.newInstance(UserAccess.class), locationId, userId);
                if (userAccess != null) {
                    String newTitle = userAccess.getTitle().equals("ADMIN") ? "READ" : "ADMIN";
                    jdbcTemplate.update("UPDATE accesses SET title = ? WHERE location_id = ? AND user_id = ?",
                    newTitle,locationId,userId);
                }
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
            return null;
        });
    }
}
