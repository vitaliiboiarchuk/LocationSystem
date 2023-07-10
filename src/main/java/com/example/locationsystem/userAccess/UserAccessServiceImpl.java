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
    public CompletableFuture<UserAccess> saveUserAccess(UserAccess userAccess) {

        log.info("Saving user access. Location ID: {}, User ID: {}",
            userAccess.getLocationId(), userAccess.getUserId());
        return userAccessDao.saveUserAccess(userAccess);
    }

    @Override
    public CompletableFuture<UserAccess> findUserAccess(UserAccess userAccess) {

        return userAccessDao.findUserAccess(userAccess);
    }

    @Override
    public CompletableFuture<Void> changeUserAccess(UserAccess userAccess) {

        log.info("Changing user access. Location ID: {}, User ID: {}",
            userAccess.getLocationId(), userAccess.getUserId());
        return userAccessDao.changeUserAccess(userAccess);
    }
}

