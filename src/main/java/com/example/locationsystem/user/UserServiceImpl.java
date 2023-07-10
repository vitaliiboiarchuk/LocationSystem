package com.example.locationsystem.user;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {

        this.userDao = userDao;
    }

    @Override
    public CompletableFuture<User> findUserByEmail(String email) {

        log.info("Finding user by email");
        return userDao.findUserByEmail(email);
    }

    @Override
    public CompletableFuture<User> findUserByEmailAndPassword(String email, String password) {

        log.info("Finding user by email and password");
        return userDao.findUserByEmailAndPassword(email, password);
    }

    @Override
    public CompletableFuture<User> saveUser(User user) {

        log.info("Saving user: {}", user);
        return userDao.saveUser(user);
    }

    @Override
    public CompletableFuture<User> findUserById(Long id) {

        return userDao.findUserById(id);
    }

    @Override
    public CompletableFuture<List<User>> findAllUsersOnLocation(Long locationId, Long userId) {

        log.info("Finding all users with access on location. Location ID: {}, User ID: {}",
            locationId, userId);
        return userDao.findAllUsersOnLocation(locationId, userId);
    }

    @Override
    public CompletableFuture<User> findLocationOwner(Long locationId, Long id) {

        log.info("Finding location owner. Location ID: {}, User ID: {}", locationId, id);
        return userDao.findLocationOwner(locationId);

    }

    @Override
    public CompletableFuture<Void> deleteUserByEmail(String email) {

        log.info("Deleting user by username: {}", email);
        return userDao.deleteUserByEmail(email);
    }

}

