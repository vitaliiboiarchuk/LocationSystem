package com.example.locationsystem.location;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Component
public class LocationDao {

    private final JdbcTemplate jdbcTemplate;

    public LocationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Async
    public CompletableFuture<List<Location>> findAllAddedLocations(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return jdbcTemplate.query("SELECT * FROM locations WHERE user_id = ?",
                        BeanPropertyRowMapper.newInstance(Location.class), id);
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }

    @Async
    public CompletableFuture<List<Location>> findAllLocationsWithAccess(Long id, String title) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return jdbcTemplate.query("SELECT locations.id,locations.name,locations.address FROM locations JOIN accesses ON locations.id = accesses.location_id WHERE accesses.user_id = ? AND accesses.title = ?",
                        BeanPropertyRowMapper.newInstance(Location.class), id, title);
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }

    @Async
    public CompletableFuture<Location> findLocationByNameAndUserId(String name, Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return jdbcTemplate.queryForObject("SELECT * FROM locations WHERE name = ? AND user_id = ?",
                        BeanPropertyRowMapper.newInstance(Location.class), name, userId);
            } catch (IncorrectResultSizeDataAccessException e) {
                return null;
            }
        });
    }

    @Async
    public CompletableFuture<Void> saveLocation(Location location) {
        return CompletableFuture.runAsync(() ->
                jdbcTemplate.update("INSERT INTO locations(name,address,user_id) VALUES (?,?,?)",
                        location.getName(), location.getAddress(), location.getUser().getId()));
    }

    @Async
    public CompletableFuture<Void> deleteLocation(Long id, Long userId) {
        CompletableFuture<Void> deleteAccessesFuture = CompletableFuture.runAsync(() ->
                jdbcTemplate.update("DELETE FROM accesses WHERE location_id = ?", id));
        CompletableFuture<Void> deleteLocationsFuture = CompletableFuture.runAsync(() ->
                jdbcTemplate.update("DELETE FROM locations WHERE id = ? AND user_id = ?", id, userId));

        return CompletableFuture.allOf(deleteAccessesFuture, deleteLocationsFuture);
    }

}
