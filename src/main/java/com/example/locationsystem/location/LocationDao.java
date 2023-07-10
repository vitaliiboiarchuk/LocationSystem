package com.example.locationsystem.location;

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
import java.util.concurrent.CompletableFuture;

@Component
@Log4j2
public class LocationDao {

    private final JdbcTemplate jdbcTemplate;

    public LocationDao(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String FIND_ALL_USER_LOCATIONS = "SELECT locations.id, locations.name, locations.address, locations.user_id " +
        "FROM " +
        "locations JOIN accesses ON locations.id = accesses.location_id WHERE accesses.user_id = ? UNION SELECT " +
        "id, name, address, user_id FROM locations WHERE user_id = ?";
    private static final String FIND_LOCATION_BY_NAME_AND_USER_ID = "SELECT * FROM locations WHERE name = ? AND " +
        "user_id = ?";
    private static final String SAVE_LOCATION = "INSERT INTO locations(name,address,user_id) VALUES (?,?,?)";
    private static final String FIND_LOCATION_BY_ID = "SELECT * FROM locations WHERE id = ?";
    private static final String FIND_NOT_SHARED_TO_USER_LOCATIONS = "SELECT l.* FROM locations l LEFT JOIN accesses a" +
        " ON l.id = a.location_id WHERE (l.user_id = ? OR (a.user_id = ? AND a.title = 'ADMIN')) AND (l.id NOT IN " +
        "(SELECT l.id FROM locations l JOIN accesses a ON l.id = a.location_id WHERE a.user_id = ? AND a.title IN " +
        "('ADMIN', 'READ')))";
    private static final String DELETE_LOCATION = "DELETE FROM locations WHERE id = ? AND user_id = ?";

    public CompletableFuture<List<Location>> findAllUserLocations(Long id) {

        return CompletableFuture.supplyAsync(() -> {
            List<Location> locations = jdbcTemplate.query(FIND_ALL_USER_LOCATIONS,
                BeanPropertyRowMapper.newInstance(Location.class), id, id);
            log.info("All user locations by user id found: {}", id);
            return locations;
        });
    }

    public CompletableFuture<Location> findLocationByNameAndUserId(String name, Long userId) {

        return CompletableFuture.supplyAsync(() ->
            jdbcTemplate.query(FIND_LOCATION_BY_NAME_AND_USER_ID,
                    BeanPropertyRowMapper.newInstance(Location.class), name, userId)
                .stream()
                .peek(location -> log.info("Location found by name and user id. Name: {}, User id: {}", name, userId))
                .findFirst()
                .orElse(null));
    }

    public CompletableFuture<Location> saveLocation(Location location) {
        return CompletableFuture.supplyAsync(() -> {

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SAVE_LOCATION, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, location.getName());
                ps.setString(2, location.getAddress());
                ps.setLong(3, location.getUserId());
                return ps;
            }, keyHolder);

            Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
            location.setId(generatedId);

            log.info("Location saved: {}", location);
            return location;
        });
    }

    public CompletableFuture<Location> findLocationById(Long id) {

        return CompletableFuture.supplyAsync(() ->
            jdbcTemplate.query(FIND_LOCATION_BY_ID,
                    BeanPropertyRowMapper.newInstance(Location.class), id)
                .stream()
                .findFirst()
                .orElse(null));
    }

    public CompletableFuture<List<Location>> findNotSharedToUserLocations(Long id, Long userId) {

        return CompletableFuture.supplyAsync(() -> {
            List<Location> locations = jdbcTemplate.query(FIND_NOT_SHARED_TO_USER_LOCATIONS,
                BeanPropertyRowMapper.newInstance(Location.class), id, id, userId);
            log.info("Found not shared to user locations with User id: {} and User to share id: {}", id, userId);
            return locations;
        });
    }

    public CompletableFuture<Void> deleteLocation(Long id, Long userId) {

        return CompletableFuture.runAsync(() -> {
            jdbcTemplate.update(DELETE_LOCATION, id, userId);
            log.info("Location deleted by location id: {} and user id: {}", id, userId);
        });
    }

}
