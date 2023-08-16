package com.example.locationsystem.user;

import com.example.locationsystem.event.ObjectChangeEvent;
import com.example.locationsystem.util.EmailUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.example.locationsystem.exception.ControllerExceptions.*;

@Service
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserDao userDao;
    EmailUtil emailUtil;
    ApplicationEventPublisher eventPublisher;

    @Override
    public CompletableFuture<Long> saveUser(User user) {

        log.info("Saving user with email={}", emailUtil.hideEmail(user.getUsername()));
        return userDao.saveUser(user)
            .thenApply(savedUserId -> {
                eventPublisher.publishEvent(new ObjectChangeEvent(this, ObjectChangeEvent.ObjectType.USER,
                    ObjectChangeEvent.ActionType.CREATED, new Timestamp(System.currentTimeMillis()), savedUserId));
                return savedUserId;
            });
    }

    @Override
    public CompletableFuture<Void> deleteUserByEmail(String email) {

        log.info("Deleting user by email={}", emailUtil.hideEmail(email));
        return userDao.findUserByEmail(email)
            .thenCompose(userOptional -> {
                if (userOptional.isPresent()) {
                    return userDao.deleteUserByEmail(email)
                        .thenAccept(result ->
                            eventPublisher.publishEvent(new ObjectChangeEvent(this,
                            ObjectChangeEvent.ObjectType.USER, ObjectChangeEvent.ActionType.DELETED,
                            new Timestamp(System.currentTimeMillis()), userOptional.get().getId())));
                } else {
                    log.warn("User not found by email={}", emailUtil.hideEmail(email));
                    throw new UserNotFoundException("User not found");
                }
            });
    }

    @Override
    public CompletableFuture<List<Long>> findAllUsersOnLocation(Long locationId, Long userId) {

        log.info("Finding all users with access on location by location id={} and user id={}",
            locationId, userId);
        return userDao.findAllUsersOnLocation(locationId, userId);
    }

    @Override
    public CompletableFuture<Optional<User>> findUserByEmail(String email) {

        log.info("Finding user by email={}", emailUtil.hideEmail(email));
        return userDao.findUserByEmail(email);
    }

    @Override
    public CompletableFuture<User> findUserByEmailAndPassword(String email, String password) {

        log.info("Finding user by email={} and password", emailUtil.hideEmail(email));
        return userDao.findUserByEmailAndPassword(email, password);
    }

    @Override
    public CompletableFuture<User> findUserById(Long id) {

        log.info("Finding user by id={}", id);
        return userDao.findUserById(id);
    }
}

