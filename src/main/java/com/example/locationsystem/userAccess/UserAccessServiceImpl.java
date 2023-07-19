package com.example.locationsystem.userAccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserAccessServiceImpl implements UserAccessService {

    private final UserAccessDao userAccessDao;

    @Override
    public CompletableFuture<UserAccess> saveUserAccess(UserAccess userAccess) {

        log.info("Saving user access by location id={}, user id={}",
            userAccess.getLocationId(), userAccess.getUserId());
        return userAccessDao.saveUserAccess(userAccess);
    }

    @Override
    public CompletableFuture<UserAccess> findUserAccess(UserAccess userAccess, Long userId) {

        log.info("Finding user access by location id={}, user to share id={}, owner id={}",
            userAccess.getLocationId(), userAccess.getUserId(), userId);
        return userAccessDao.findUserAccess(userAccess, userId);
    }

    @Override
    public CompletableFuture<Void> changeUserAccess(UserAccess userAccess) {

        log.info("Changing user access by location id={}, user id={}",
            userAccess.getLocationId(), userAccess.getUserId());
        return userAccessDao.changeUserAccess(userAccess);
    }
}

