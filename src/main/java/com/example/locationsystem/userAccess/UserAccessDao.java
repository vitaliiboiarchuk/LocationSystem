package com.example.locationsystem.userAccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class UserAccessDao {

    private final Logger log = LoggerFactory.getLogger(UserAccessDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserAccessDao(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    public CompletableFuture<Void> saveUserAccess(UserAccess userAccess) {

        return CompletableFuture.runAsync(() -> {
            jdbcTemplate.update("INSERT INTO accesses(title,location_id,user_id) VALUES(?,?,?)",
                userAccess.getTitle(), userAccess.getLocation().getId(), userAccess.getUser().getId());
            log.info("User access saved: {}", userAccess);
        });
    }

    public CompletableFuture<UserAccess> findUserAccess(Long locationId, Long userId) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return jdbcTemplate.queryForObject("SELECT * FROM accesses WHERE location_id = ? AND" +
                        " user_id = ?",
                    BeanPropertyRowMapper.newInstance(UserAccess.class), locationId, userId);
            } catch (IncorrectResultSizeDataAccessException e) {
                log.warn("User access not found for locationId: {} and userId: {}", locationId, userId);
                return null;
            }
        });
    }

    public CompletableFuture<Void> changeUserAccess(String newTitle, Long locationId, Long userId) {

        return CompletableFuture.runAsync(() -> {
            jdbcTemplate.update("UPDATE accesses SET title = ? WHERE location_id = ? AND user_id = ?",
                newTitle, locationId, userId);
            log.info("User access changed - new title: {}, locationId: {}, userId: {}",
                newTitle, locationId, userId);
        });
    }
}
