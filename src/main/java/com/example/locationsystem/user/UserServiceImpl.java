package com.example.locationsystem.user;

import com.example.locationsystem.event.EventService;
import com.example.locationsystem.utils.EmailUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserDao userDao;
    EmailUtils emailUtils;
    EventService eventService;

    @Override
    public CompletableFuture<Optional<User>> findUserByEmail(String email) {

        log.info("Finding user by email={}", emailUtils.hideEmail(email));
        return userDao.findUserByEmail(email);
    }

    @Override
    public CompletableFuture<User> findUserByEmailAndPassword(String email, String password) {

        log.info("Finding user by email={} and password", emailUtils.hideEmail(email));
        return userDao.findUserByEmailAndPassword(email, password);
    }

    @Override
    public CompletableFuture<User> saveUser(User user) {

        log.info("Saving user with email={}", emailUtils.hideEmail(user.getUsername()));
        return userDao.saveUser(user)
            .thenApply(savedUser -> {
                eventService.publishUserCreatedEvent(savedUser);
                return savedUser;
            });
    }

    @Override
    public CompletableFuture<List<User>> findAllUsersOnLocation(Long locationId, Long userId) {

        log.info("Finding all users with access on location by location id={} and user id={}",
            locationId, userId);
        return userDao.findAllUsersOnLocation(locationId, userId);
    }

    @Override
    public CompletableFuture<Void> deleteUserByEmail(String email) {

        log.info("Deleting user by email={}", emailUtils.hideEmail(email));
        return userDao.deleteUserByEmail(email)
            .thenAccept(optionalDeletedUser ->
                optionalDeletedUser.ifPresent(eventService::publishUserDeletedEvent));
    }

    @Override
    public CompletableFuture<User> findLocationOwner(String locationName, Long ownerId) {

        log.info("Finding location owner by location name={}, owner id={}", locationName, ownerId);
        return userDao.findLocationOwner(locationName, ownerId);
    }

    @Override
    public CompletableFuture<User> findUserById(Long id) {

        log.info("Finding user by id={}", id);
        return userDao.findUserById(id);
    }
}

