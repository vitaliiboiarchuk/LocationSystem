package com.example.locationsystem.userAccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
@Log4j2
@RequiredArgsConstructor
public class UserAccessDao {

    private final JdbcTemplate jdbcTemplate;

    private static final String SAVE_USER_ACCESS = "INSERT INTO accesses(title,location_id,user_id) VALUES(?,?,?)";
    private static final String FIND_USER_ACCESS = "SELECT * FROM accesses WHERE location_id = ? AND user_id = ?";
    private static final String CHANGE_USER_ACCESS = "UPDATE accesses SET title = CASE WHEN title = 'ADMIN' THEN " +
        "'READ' WHEN title = 'READ' THEN 'ADMIN' ELSE title END WHERE location_id = ? AND user_id = ?";

    public CompletableFuture<UserAccess> saveUserAccess(UserAccess userAccess) {

        return CompletableFuture.supplyAsync(() -> {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SAVE_USER_ACCESS, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, userAccess.getTitle());
                ps.setLong(2, userAccess.getLocationId());
                ps.setLong(3, userAccess.getUserId());
                return ps;
            }, keyHolder);
            Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
            userAccess.setId(generatedId);
            log.info("User access saved={}", userAccess);
            return userAccess;
        });
    }

    public CompletableFuture<UserAccess> findUserAccess(UserAccess userAccess) {

        return CompletableFuture.supplyAsync(() ->
            jdbcTemplate.query(FIND_USER_ACCESS,
                    BeanPropertyRowMapper.newInstance(UserAccess.class), userAccess.getLocationId(),
                    userAccess.getUserId())
                .stream()
                .findFirst()
                .orElse(null));
    }

    public CompletableFuture<Void> changeUserAccess(UserAccess userAccess) {

        return CompletableFuture.runAsync(() -> {
            jdbcTemplate.update(CHANGE_USER_ACCESS, userAccess.getLocationId(), userAccess.getUserId());
            log.info("User access changed. Location ID={}, User ID={}",
                userAccess.getLocationId(), userAccess.getUserId());
        });
    }
}