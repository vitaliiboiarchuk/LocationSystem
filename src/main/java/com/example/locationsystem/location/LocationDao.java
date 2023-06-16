package com.example.locationsystem.location;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LocationDao {

    private final JdbcTemplate jdbcTemplate;

    public LocationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Location> findAllAddedLocations(Long id) {
        try {
            return jdbcTemplate.query("SELECT * FROM locations WHERE user_id = ?",
                    BeanPropertyRowMapper.newInstance(Location.class),id);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    public List<Location> findAllLocationsWithAccess(Long id, String title) {
        try {
            return jdbcTemplate.query("SELECT locations.id,locations.name,locations.address FROM locations JOIN accesses ON locations.id = accesses.location_id WHERE accesses.user_id = ? AND accesses.title = ?",
                    BeanPropertyRowMapper.newInstance(Location.class),id,title);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    public Location findLocationByNameAndUserId(String name, Long userId) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM locations WHERE name = ? AND user_id = ?",
                    BeanPropertyRowMapper.newInstance(Location.class),name,userId);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    public void saveLocation(Location location) {
        jdbcTemplate.update("INSERT INTO locations(name,address,user_id) VALUES (?,?,?)",
                location.getName(),location.getAddress(),location.getUser().getId());
    }

    public List<Location> findNotSharedToUserLocations(Long id, Long userId) {
        List<Location> locationsToShare = Stream.of(
                        findAllAddedLocations(id),
                        findAllLocationsWithAccess(id,"ADMIN")
                )
                .flatMap(Collection::stream).collect(Collectors.toList());

        List<Location> allLocationsOfUser = Stream.of(
                        findAllAddedLocations(userId),
                        findAllLocationsWithAccess(userId,"ADMIN"),
                        findAllLocationsWithAccess(userId,"READ")
                )
                .flatMap(Collection::stream).collect(Collectors.toList());

        for (Location location : allLocationsOfUser) {
            locationsToShare.remove(location);
        }
        return locationsToShare;
    }

    public void deleteLocation(Long id, Long userId) {
        jdbcTemplate.update("DELETE FROM accesses WHERE location_id = ?",id);
        jdbcTemplate.update("DELETE FROM locations WHERE id = ? AND user_id = ?",id,userId);
    }

}
