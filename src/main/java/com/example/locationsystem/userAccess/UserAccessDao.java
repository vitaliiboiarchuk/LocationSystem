package com.example.locationsystem.userAccess;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.example.locationsystem.exception.ControllerExceptions.*;

@Component
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAccessDao {

    JdbcTemplate jdbcTemplate;

    private static final String SAVE_USER_ACCESS = "INSERT INTO accesses(title,location_id,user_id) VALUES(?,?,?)";
    private static final String FIND_USER_ACCESS = "SELECT a.* FROM accesses a INNER JOIN locations l ON a" +
        ".location_id = l.id WHERE a.location_id = ? AND a.user_id = ? AND l.user_id = ?;";
    private static final String CHANGE_USER_ACCESS = "UPDATE accesses SET title = CASE WHEN title = 'ADMIN' THEN " +
        "'READ' WHEN title = 'READ' THEN 'ADMIN' ELSE title END WHERE location_id = ? AND user_id = ?";

    public CompletableFuture<UserAccess> saveUserAccess(UserAccess userAccess) {

        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
                PreparedStatement ps = connection.prepareStatement(SAVE_USER_ACCESS, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, userAccess.getTitle());
                ps.setLong(2, userAccess.getLocationId());
                ps.setLong(3, userAccess.getUserId());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    userAccess.setId(rs.getLong(1));
                }
                log.info("User access={} saved", userAccess);
                return userAccess;
            } catch (SQLException e) {
                throw new UserAccessSaveException("Failed to save user access");
            }
        });
    }

    public CompletableFuture<UserAccess> findUserAccess(UserAccess userAccess, Long ownerId) {

        return CompletableFuture.supplyAsync(() ->
            jdbcTemplate.query(FIND_USER_ACCESS, BeanPropertyRowMapper.newInstance(UserAccess.class),
                    userAccess.getLocationId(), userAccess.getUserId(), ownerId)
                .stream()
                .peek(access -> log.info("User access found by location id={}, user id={}, owner id={}",
                    userAccess.getLocationId(), userAccess.getUserId(), ownerId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("User access not found by location id={}, user id={}, owner id={}",
                        userAccess.getLocationId(), userAccess.getUserId(), ownerId);
                    throw new UserAccessNotFoundException("User access not found");
                }));
    }

    public CompletableFuture<Void> changeUserAccess(UserAccess userAccess) {

        return CompletableFuture.runAsync(() -> {
            jdbcTemplate.update(CHANGE_USER_ACCESS, userAccess.getLocationId(), userAccess.getUserId());
            log.info("User access changed by location id={}, user id={}",
                userAccess.getLocationId(), userAccess.getUserId());
        });
    }
}