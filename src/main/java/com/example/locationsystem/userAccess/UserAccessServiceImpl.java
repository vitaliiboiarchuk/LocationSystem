package com.example.locationsystem.userAccess;

import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class UserAccessServiceImpl implements UserAccessService {

    private final UserAccessDao userAccessDao;

    public UserAccessServiceImpl(UserAccessDao userAccessDao) {
        this.userAccessDao = userAccessDao;
    }

    @Override
    public CompletableFuture<Void> saveUserAccess(UserAccess userAccess) {

        return userAccessDao.saveUserAccess(userAccess);
    }

    @Override
    public CompletableFuture<Void> changeUserAccess(Long locationId, Long userId) {
        return userAccessDao.changeUserAccess(locationId,userId);
    }

}
