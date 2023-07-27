package com.example.locationsystem.location;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class LocationDao {

    JdbcTemplate jdbcTemplate;

    private static final String FIND_ALL_USER_LOCATIONS = "SELECT locations.id, locations.name, locations.address, " +
        "locations.user_id " +
        "FROM " +
        "locations JOIN accesses ON locations.id = accesses.location_id WHERE accesses.user_id = ? UNION SELECT " +
        "id, name, address, user_id FROM locations WHERE user_id = ?";
    private static final String FIND_LOCATION_BY_NAME_AND_USER_ID = "SELECT * FROM locations WHERE name = ? AND " +
        "user_id = ?";
    private static final String SAVE_LOCATION = "INSERT INTO locations(name,address,user_id) VALUES (?,?,?)";
    private static final String FIND_NOT_SHARED_TO_USER_LOCATION = "SELECT l.* FROM locations l LEFT JOIN accesses a" +
        " ON l.id = a.location_id WHERE (l.user_id = ? AND l.id = ? OR (a.user_id = ? AND a.title = 'ADMIN' AND a" +
        ".location_id = ?)) AND (l.id NOT IN " +
        "(SELECT l.id FROM locations l JOIN accesses a ON l.id = a.location_id WHERE a.user_id = ? AND a.title IN " +
        "('ADMIN', 'READ'))) AND EXISTS (SELECT 1 FROM users u WHERE u.id = ?);";
    private static final String DELETE_LOCATION = "DELETE FROM locations WHERE name = ? AND user_id = ?";
    private static final String FIND_LOCATION_IN_USER_LOCATIONS = "SELECT locations.id, locations.name, locations" +
        ".address, locations.user_id FROM locations JOIN accesses ON locations.id = accesses.location_id WHERE " +
        "accesses.user_id = ? AND locations.id = ? UNION SELECT id, name, address, user_id FROM locations WHERE " +
        "user_id = ? AND locations.id = ?";
    private static final String FIND_LOCATION_BY_ID = "SELECT * FROM locations WHERE id = ?";

    public CompletableFuture<List<Location>> findAllUserLocations(Long id) {

        return CompletableFuture.supplyAsync(() -> {
            List<Location> locations = jdbcTemplate.query(FIND_ALL_USER_LOCATIONS,
                BeanPropertyRowMapper.newInstance(Location.class), id, id);
            log.info("All user locations by user id={} found", id);
            return locations;
        });
    }

    public CompletableFuture<Location> findLocationInUserLocations(Long userId, Long locationId) {

        return CompletableFuture.supplyAsync(() ->
            jdbcTemplate.query(FIND_LOCATION_IN_USER_LOCATIONS,
                    BeanPropertyRowMapper.newInstance(Location.class), userId, locationId, userId, locationId)
                .stream()
                .peek(loc -> log.info("Location found in user locations by user id={} and location id={}",
                    userId, locationId))
                .findFirst()
                .orElseThrow(() -> {
                        log.warn("Location not found in user locations by user id={} and location id={}", userId,
                            locationId);
                        throw new LocationNotFoundException("Location not found");
                    }
                ));
    }

    public CompletableFuture<Optional<Location>> findLocationByNameAndUserId(String name, Long userId) {

        return CompletableFuture.supplyAsync(() ->
            jdbcTemplate.query(FIND_LOCATION_BY_NAME_AND_USER_ID,
                    BeanPropertyRowMapper.newInstance(Location.class), name, userId)
                .stream()
                .peek(location -> log.info("Location found by name={} and user id={}", name, userId))
                .findFirst());
    }

    public CompletableFuture<Location> saveLocation(Location location) {

        return CompletableFuture.supplyAsync(() -> {

            try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {

                PreparedStatement ps = connection.prepareStatement(SAVE_LOCATION, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, location.getName());
                ps.setString(2, location.getAddress());
                ps.setLong(3, location.getUserId());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    location.setId(rs.getLong(1));
                }
                log.info("Location saved={}", location);
                return location;

            } catch (SQLException e) {
                throw new RuntimeException("Failed to save location", e);
            }
        });
    }

    public CompletableFuture<Location> findNotSharedToUserLocation(Long id, Long locId, Long userId) {

        return CompletableFuture.supplyAsync(() ->
            jdbcTemplate.query(FIND_NOT_SHARED_TO_USER_LOCATION,
                    BeanPropertyRowMapper.newInstance(Location.class), id, locId, id, locId, userId, userId)
                .stream()
                .peek(loc -> log.info("Found not shared to user location by owner id={}, location id={}, user to " +
                    "share id={}", id, locId, userId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Location or user not found by owner id={}, location id={}, user to share id={}",
                        id, locId, userId);
                    throw new LocationOrUserNotFoundException("Location or user not found");
                }));
    }

    public CompletableFuture<Void> deleteLocation(String name, Long userId) {

        return CompletableFuture.runAsync(() -> {
            jdbcTemplate.update(DELETE_LOCATION, name, userId);
            log.info("Location deleted by location name={} and user id={}", name, userId);
        });
    }

    public CompletableFuture<Location> findLocationById(Long id) {

        return CompletableFuture.supplyAsync(() ->
            jdbcTemplate.query(FIND_LOCATION_BY_ID,
                    BeanPropertyRowMapper.newInstance(Location.class), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Location not found by id={}", id);
                    throw new LocationNotFoundException("Location not found");
                }));
    }
}
