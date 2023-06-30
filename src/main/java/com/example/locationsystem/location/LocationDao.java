package com.example.locationsystem.location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class LocationDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationDao.class);

    private final JdbcTemplate jdbcTemplate;

    public LocationDao(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    public CompletableFuture<List<Location>> findAllAddedLocations(Long id) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Location> locations = jdbcTemplate.query("SELECT * FROM locations WHERE user_id = ?",
                    BeanPropertyRowMapper.newInstance(Location.class), id);
                LOGGER.info("All added locations by user id found: {}", id);
                return locations;
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }

    public CompletableFuture<List<Location>> findAllLocationsWithAccess(Long id, String title) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Location> locations = jdbcTemplate.query("SELECT locations.id,locations.name,locations.address " +
                        "FROM locations JOIN " +
                        "accesses ON locations.id = accesses.location_id WHERE accesses.user_id = ? AND accesses" +
                        ".title = ?",
                    BeanPropertyRowMapper.newInstance(Location.class), id, title);
                LOGGER.info("All locations with access by user id and title found. User id: {}, Title: {}", id, title);
                return locations;
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }

    public CompletableFuture<Location> findLocationByNameAndUserId(String name, Long userId) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                Location location = jdbcTemplate.queryForObject("SELECT * FROM locations WHERE name = ? AND user_id =" +
                        " ?",
                    BeanPropertyRowMapper.newInstance(Location.class), name, userId);
                LOGGER.info("Found location by name and user id. Name: {}, User id: {}", name, userId);
                return location;
            } catch (IncorrectResultSizeDataAccessException e) {
                LOGGER.warn("Location by name and user id not found. Name: {}, User id: {}", name, userId);
                return null;
            }
        });
    }

    public CompletableFuture<Void> saveLocation(Location location) {

        return CompletableFuture.runAsync(() -> {
            jdbcTemplate.update("INSERT INTO locations(name,address,user_id) VALUES (?,?,?)",
                location.getName(), location.getAddress(), location.getUser().getId());
            LOGGER.info("Location saved: {}", location);
        });
    }

    public CompletableFuture<Void> deleteLocation(Long id, Long userId) {

        CompletableFuture<Void> deleteAccessesFuture = CompletableFuture.runAsync(() ->
            jdbcTemplate.update("DELETE FROM accesses WHERE location_id = ?", id));
        CompletableFuture<Void> deleteLocationsFuture = deleteAccessesFuture.thenComposeAsync((Void) ->
            CompletableFuture.runAsync(() ->
                jdbcTemplate.update("DELETE FROM locations WHERE id = ? AND user_id = ?", id, userId)));
        LOGGER.info("Location deleted by location id: {} and user id: {}", id, userId);
        return CompletableFuture.allOf(deleteAccessesFuture, deleteLocationsFuture);
    }

    public CompletableFuture<List<Location>> findNotSharedToUserLocations(Long id, Long userId) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Location> locations = jdbcTemplate.query("SELECT l.* FROM locations l LEFT JOIN accesses a ON l" +
                        ".id = a.location_id " +
                        "WHERE (l.user_id = ? OR (a.user_id = ? AND a.title = 'ADMIN')) " +
                        "AND (l.id NOT IN (" +
                        "SELECT l.id FROM locations l JOIN accesses a ON l.id = a.location_id " +
                        "WHERE a.user_id = ? AND a.title IN ('ADMIN', 'READ')))",
                    BeanPropertyRowMapper.newInstance(Location.class), id, id, userId);
                LOGGER.info("Found not shared to user locations with User id: {} and User to share id: {}", id, userId);
                return locations;
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }

    public CompletableFuture<Location> findById(Long id) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return jdbcTemplate.queryForObject("SELECT * FROM locations WHERE id = ?",
                    BeanPropertyRowMapper.newInstance(Location.class), id);
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }

    public CompletableFuture<Location> findLocationByName(String name) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return jdbcTemplate.queryForObject("SELECT * FROM locations WHERE name = ?",
                    BeanPropertyRowMapper.newInstance(Location.class), name);
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }
}
