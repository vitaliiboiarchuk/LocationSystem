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
        return userDao.findUserByUsernameAndPassword(username,password);
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
    public CompletableFuture<List<User>> findUsersToShare(Long id) {
        return userDao.findUsersToShare(id);
    }

    @Override
    public CompletableFuture<List<User>> findAllUsersWithAccessOnLocation(Long locationId, String title, Long id) {
        return userDao.findAllUsersWithAccessOnLocation(locationId,title,id);
    }

    @Override
    public CompletableFuture<User> findLocationOwner(Long locationId, Long id) {
        return userDao.findLocationOwner(locationId,id);
    }
}

