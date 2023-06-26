package com.example.locationsystem.userAccess;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class UserAccessServiceImpl implements UserAccessService {

    private final UserAccessDao userAccessDao;

    public UserAccessServiceImpl(UserAccessDao userAccessDao) {

        this.userAccessDao = userAccessDao;
    }

    @Override
    public CompletableFuture<Void> saveUserAccess(UserAccess userAccess) {

        log.info("Saving user access: {}", userAccess);
        return userAccessDao.saveUserAccess(userAccess);
    }

    @Override
    public CompletableFuture<UserAccess> findUserAccess(Long locationId, Long userId) {

        return userAccessDao.findUserAccess(locationId, userId);
    }

    @Override
    public CompletableFuture<Void> changeUserAccess(Long locationId, Long userId) {

        log.info("Changing user access for locationId: {} and userId: {}", locationId, userId);
        CompletableFuture<UserAccess> userAccessFuture = findUserAccess(locationId, userId);
        UserAccess userAccess = userAccessFuture.join();

        String newTitle = userAccess.getTitle().equals("ADMIN") ? "READ" : "ADMIN";
        return userAccessDao.changeUserAccess(newTitle, locationId, userId);
    }
}

