package com.example.locationsystem.user;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User findByUsername(String username) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users WHERE username = ?",
                    BeanPropertyRowMapper.newInstance(User.class), username);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    public User findUserByUsernameAndPassword(String username, String password) {
        try {
            String hashedPassword = jdbcTemplate.queryForObject("SELECT SHA2(?, 256)", String.class, password);
            return jdbcTemplate.queryForObject("SELECT * FROM users WHERE username = ? and password = ?",
                    BeanPropertyRowMapper.newInstance(User.class), username, hashedPassword);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    public void saveUser(User user) {
        String hashedPassword = jdbcTemplate.queryForObject("SELECT SHA2(?, 256)", String.class, user.getPassword());
        jdbcTemplate.update("INSERT INTO users (name,password,username) VALUES (?,?,?)",
                user.getName(), hashedPassword, user.getUsername());
    }

    public User findById(Long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?",
                    BeanPropertyRowMapper.newInstance(User.class),id);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    public List<User> findUsersToShare(Long id) {
        try {
            return jdbcTemplate.query("SELECT * FROM users WHERE id != ?",
                    BeanPropertyRowMapper.newInstance(User.class), id);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    public List<User> findAllUsersWithAccessOnLocation(Long locationId, String title, Long id) {
        try {
            List<User> users = jdbcTemplate.query("SELECT users.id,users.name,users.username FROM users JOIN accesses ON users.id = accesses.user_id WHERE accesses.location_id = ? AND accesses.title = ?",
                    BeanPropertyRowMapper.newInstance(User.class), locationId, title);
            users.removeIf(user -> user.getId().equals(id));
            return users;
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    public User findLocationOwner(Long locationId, Long id) {
        try {
            Long ownerId = jdbcTemplate.queryForObject("SELECT user_id FROM locations WHERE id = ?",
                    Long.class,locationId);
            User owner = findById(ownerId);
            if (owner.getId().equals(id)) {
                return null;
            }
            return owner;
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }
}
