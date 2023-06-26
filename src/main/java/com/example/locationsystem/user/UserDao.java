package com.example.locationsystem.user;

import lombok.extern.log4j.Log4j2;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Log4j2
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    public CompletableFuture<User> findByUsername(String username) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                User user = jdbcTemplate.queryForObject("SELECT * FROM users WHERE username = ?",
                    BeanPropertyRowMapper.newInstance(User.class), username);
                log.info("User found by username");
                return user;
            } catch (IncorrectResultSizeDataAccessException e) {
                log.warn("User not found by username");
                return null;
            }
        });
    }

    public CompletableFuture<User> findUserByUsernameAndPassword(String username, String password) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                String hashedPassword = jdbcTemplate.queryForObject("SELECT SHA2(?, 256)", String.class, password);
                User user = jdbcTemplate.queryForObject("SELECT * FROM users WHERE username = ? and password = ?",
                    BeanPropertyRowMapper.newInstance(User.class), username, hashedPassword);
                log.info("User found by username and password");
                return user;
            } catch (IncorrectResultSizeDataAccessException e) {
                log.warn("User not found by username and password");
                return null;
            }
        });
    }

    public CompletableFuture<Void> saveUser(User user) {

        return CompletableFuture.runAsync(() -> {
            String hashedPassword = jdbcTemplate.queryForObject("SELECT SHA2(?, 256)", String.class,
                user.getPassword());
            jdbcTemplate.update("INSERT INTO users (name,password,username) VALUES (?,?,?)",
                user.getName(), hashedPassword, user.getUsername());
            log.info("User saved: {}", user);
        });
    }

    public CompletableFuture<User> findById(Long id) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?",
                    BeanPropertyRowMapper.newInstance(User.class), id);
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }

    public CompletableFuture<List<User>> findAllUsersWithAccessOnLocation(Long locationId, String title, Long userId) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<User> users = jdbcTemplate.query("SELECT users.id,users.name,users.username FROM users JOIN " +
                        "accesses ON " +
                        "users.id = accesses.user_id WHERE accesses.location_id = ? AND accesses.title = ? AND users" +
                        ".id != ?",
                    BeanPropertyRowMapper.newInstance(User.class), locationId, title, userId);
                log.info("Found all users with access on location. Location ID: {}, Title: {}, User ID: {}",
                    locationId, title, userId);
                return users;
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }

    public CompletableFuture<User> findLocationOwner(Long locationId) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                Long ownerId = jdbcTemplate.queryForObject("SELECT user_id FROM locations WHERE id = ?",
                    Long.class, locationId);
                User owner = findById(ownerId).join();
                log.info("Location owner found. Location ID: {}", locationId);
                return owner;
            } catch (IncorrectResultSizeDataAccessException e) {
                log.warn("Location owner not found. Location ID: {}", locationId);
                return null;
            }
        });
    }
}
