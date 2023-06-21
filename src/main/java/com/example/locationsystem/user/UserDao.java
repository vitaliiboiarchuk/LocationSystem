package com.example.locationsystem.user;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Async
    public CompletableFuture<User> findByUsername(String username) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return jdbcTemplate.queryForObject("SELECT * FROM users WHERE username = ?",
                    BeanPropertyRowMapper.newInstance(User.class), username);
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }

    @Async
    public CompletableFuture<User> findUserByUsernameAndPassword(String username, String password) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                String hashedPassword = jdbcTemplate.queryForObject("SELECT SHA2(?, 256)", String.class, password);
                return jdbcTemplate.queryForObject("SELECT * FROM users WHERE username = ? and password = ?",
                    BeanPropertyRowMapper.newInstance(User.class), username, hashedPassword);
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }

    @Async
    public CompletableFuture<Void> saveUser(User user) {

        return CompletableFuture.runAsync(() -> {
            String hashedPassword = jdbcTemplate.queryForObject("SELECT SHA2(?, 256)", String.class,
                user.getPassword());
            jdbcTemplate.update("INSERT INTO users (name,password,username) VALUES (?,?,?)",
                user.getName(), hashedPassword, user.getUsername());
        });
    }

    @Async
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

    @Async
    public CompletableFuture<List<User>> findAllUsersWithAccessOnLocation(Long locationId, String title, Long userId) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return jdbcTemplate.query("SELECT users.id,users.name,users.username FROM users JOIN accesses ON " +
                        "users.id = accesses.user_id WHERE accesses.location_id = ? AND accesses.title = ? AND users" +
                        ".id != ?",
                    BeanPropertyRowMapper.newInstance(User.class), locationId, title, userId);
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }

    @Async
    public CompletableFuture<User> findLocationOwner(Long locationId) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                Long ownerId = jdbcTemplate.queryForObject("SELECT user_id FROM locations WHERE id = ?",
                    Long.class, locationId);
                return findById(ownerId).join();
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }
}
