package com.example.locationsystem.user;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {

        this.userDao = userDao;
    }

    @Override
    public CompletableFuture<User> findByUsername(String username) {

        return userDao.findByUsername(username);
    }

    @Override
    public CompletableFuture<User> findUserByUsernameAndPassword(String username, String password) {

        return userDao.findUserByUsernameAndPassword(username, password);
    }

    @Override
    public CompletableFuture<Void> saveUser(User user) {

        return userDao.saveUser(user);
    }

    @Override
    public CompletableFuture<User> findById(Long id) {

        return userDao.findById(id);
    }

    @Override
    public CompletableFuture<List<User>> findAllUsersWithAccessOnLocation(Long locationId, String title, Long userId) {

        return userDao.findAllUsersWithAccessOnLocation(locationId, title, userId);
    }

    @Override
    public CompletableFuture<User> findLocationOwner(Long locationId, Long id) {

        CompletableFuture<User> owner = userDao.findLocationOwner(locationId);
        return owner.thenApplyAsync(result -> {
            if (result != null && result.getId().equals(id)) {
                return null;
            }
            return result;
        });
    }
}

