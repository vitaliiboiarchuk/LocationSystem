package com.example.locationsystem.userAccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class UserAccessServiceImpl implements UserAccessService {

    private final Logger LOGGER = LoggerFactory.getLogger(UserAccessServiceImpl.class);

    private final UserAccessDao userAccessDao;

    public UserAccessServiceImpl(UserAccessDao userAccessDao) {

        this.userAccessDao = userAccessDao;
    }

    @Override
    public CompletableFuture<Void> saveUserAccess(UserAccess userAccess) {

        LOGGER.info("Saving user access: {}", userAccess);
        return userAccessDao.saveUserAccess(userAccess);
    }

    @Override
    public CompletableFuture<UserAccess> findUserAccess(Long locationId, Long userId) {

        return userAccessDao.findUserAccess(locationId, userId);
    }

    @Override
    public CompletableFuture<Void> changeUserAccess(Long locationId, Long userId) {

        LOGGER.info("Changing user access for locationId: {} and userId: {}", locationId, userId);
        CompletableFuture<UserAccess> userAccessFuture = findUserAccess(locationId, userId);
        UserAccess userAccess = userAccessFuture.join();

        String newTitle = userAccess.getTitle().equals("ADMIN") ? "READ" : "ADMIN";
        return userAccessDao.changeUserAccess(newTitle, locationId, userId);
    }
}

