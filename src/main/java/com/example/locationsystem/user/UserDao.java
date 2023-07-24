package com.example.locationsystem.user;

import com.example.locationsystem.utils.EmailUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.example.locationsystem.exception.ControllerExceptions.*;

@Component
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDao {

    JdbcTemplate jdbcTemplate;
    EmailUtils emailUtils;

    private static final String FIND_USER_BY_EMAIL = "SELECT * FROM users WHERE username = ?";
    private static final String FIND_USER_BY_EMAIL_AND_PASSWORD = "SELECT * FROM users WHERE username = ? and " +
        "password = ?";
    private static final String SAVE_USER = "INSERT INTO users (name,password,username) VALUES (?,?,?)";
    private static final String DELETE_USER_BY_EMAIL = "DELETE FROM users WHERE username = ?";
    private static final String FIND_ALL_USERS_ON_LOCATION = "SELECT users.id,users.name,users.username,users" +
        ".password FROM users JOIN accesses ON users.id = accesses.user_id WHERE accesses.location_id = ? AND users" +
        ".id != ?";
    private static final String FIND_LOCATION_OWNER = "SELECT u.id, u.name, u.password, u.username FROM users u JOIN " +
        "locations l ON u.id = l.user_id WHERE l.name = ? AND u.id = ?;";
    private static final String FIND_USER_BY_ID = "SELECT * FROM users WHERE id = ?";

    public CompletableFuture<Optional<User>> findUserByEmail(String email) {

        return CompletableFuture.supplyAsync(() ->
            jdbcTemplate.query(FIND_USER_BY_EMAIL, BeanPropertyRowMapper.newInstance(User.class), email)
                .stream()
                .peek(user -> log.info("User found by email={}", emailUtils.hideEmail(email)))
                .findFirst());
    }

    public CompletableFuture<User> findUserByEmailAndPassword(String email, String password) {

        return CompletableFuture.supplyAsync(() -> {
            String hashedPassword = jdbcTemplate.queryForObject("SELECT SHA2(?, 256)", String.class, password);
            return jdbcTemplate.query(FIND_USER_BY_EMAIL_AND_PASSWORD, BeanPropertyRowMapper.newInstance(User.class),
                    email, hashedPassword)
                .stream()
                .peek(user -> log.info("User found by email={} and password", emailUtils.hideEmail(email)))
                .findFirst()
                .orElseThrow(() -> {
                        log.warn("User not found by email={} and password", emailUtils.hideEmail(email));
                        throw new InvalidLoginOrPasswordException("Invalid login or password");
                    }
                );
        });
    }

    public CompletableFuture<User> saveUser(User user) {

        return CompletableFuture.supplyAsync(() -> {

            String hashedPassword = jdbcTemplate.queryForObject("SELECT SHA2(?, 256)", String.class,
                user.getPassword());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SAVE_USER, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getName());
                ps.setString(2, hashedPassword);
                ps.setString(3, user.getUsername());
                return ps;
            }, keyHolder);

            Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();

            user.setId(generatedId);
            log.info("User with email={} saved", emailUtils.hideEmail(user.getUsername()));
            return user;
        });
    }

    public CompletableFuture<Void> deleteUserByEmail(String email) {

        return CompletableFuture.runAsync(() -> {
            jdbcTemplate.update(DELETE_USER_BY_EMAIL, email);
            log.info("User deleted by email={}", emailUtils.hideEmail(email));
        });
    }

    public CompletableFuture<List<User>> findAllUsersOnLocation(Long locationId, Long userId) {

        return CompletableFuture.supplyAsync(() -> {
            List<User> users = jdbcTemplate.query(FIND_ALL_USERS_ON_LOCATION,
                BeanPropertyRowMapper.newInstance(User.class), locationId, userId);
            log.info("Found all users with access on location by location id={} and user id={}",
                locationId, userId);
            return users;
        });
    }

    public CompletableFuture<User> findLocationOwner(String locationName, Long ownerId) {

        return CompletableFuture.supplyAsync(() ->
            jdbcTemplate.query(FIND_LOCATION_OWNER, BeanPropertyRowMapper.newInstance(User.class),
                    locationName,
                    ownerId)
                .stream()
                .peek(owner -> log.info("Location owner found by location name={}, owner id={}",
                    locationName, ownerId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Location owner not found by location name={}, owner id={}", locationName,
                        ownerId);
                    throw new LocationOwnerNotFoundException("Location owner not found");
                }));
    }

    public CompletableFuture<User> findUserById(Long id) {

        return CompletableFuture.supplyAsync(() ->
            jdbcTemplate.query(FIND_USER_BY_ID,
                    BeanPropertyRowMapper.newInstance(User.class), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("User not found by id={}", id);
                    throw new UserNotFoundException("User not found");
                }));
    }
}
