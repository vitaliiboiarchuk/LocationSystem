package com.example.locationsystem.user;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Log4j2
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {

        this.userDao = userDao;
    }

    @Override
    public CompletableFuture<User> findByUsername(String username) {

        log.info("Finding user by username");
        return userDao.findByUsername(username);
    }

    @Override
    public CompletableFuture<User> findUserByUsernameAndPassword(String username, String password) {

        log.info("Finding user by username and password");
        return userDao.findUserByUsernameAndPassword(username, password);
    }

    @Override
    public CompletableFuture<Void> saveUser(User user) {

        log.info("Saving user: {}", user);
        return userDao.saveUser(user);
    }

    @Override
    public CompletableFuture<User> findById(Long id) {

        return userDao.findById(id);
    }

    @Override
    public CompletableFuture<List<User>> findAllUsersWithAccessOnLocation(Long locationId, Long userId) {

        log.info("Finding all users with access on location. Location ID: {}, User ID: {}",
            locationId, userId);
        CompletableFuture<List<User>> adminAccessFuture = userDao.findAllUsersWithAccessOnLocation(locationId,
            "ADMIN", userId);
        CompletableFuture<List<User>> readAccessFuture = userDao.findAllUsersWithAccessOnLocation(locationId,
            "READ", userId);

        return CompletableFuture.allOf(adminAccessFuture, readAccessFuture)
            .thenApplyAsync((Void) -> Stream.of(
                adminAccessFuture.join(),
                readAccessFuture.join()
            ).flatMap(List::stream).collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<User> findLocationOwner(Long locationId, Long id) {

        log.info("Finding location owner. Location ID: {}, User ID: {}", locationId, id);
        CompletableFuture<User> owner = userDao.findLocationOwner(locationId);
        return owner.thenApplyAsync(result -> {
            if (result != null && !result.getId().equals(id)) {
                return null;
            }
            return result;
        });
    }
}

