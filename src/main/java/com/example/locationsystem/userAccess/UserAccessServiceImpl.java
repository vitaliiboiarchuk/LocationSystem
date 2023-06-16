package com.example.locationsystem.userAccess;

import org.springframework.stereotype.Service;

@Service
public class UserAccessServiceImpl implements UserAccessService {

    private final UserAccessDao userAccessDao;

    public UserAccessServiceImpl(UserAccessDao userAccessDao) {
        this.userAccessDao = userAccessDao;
    }

    @Override
    public void saveUserAccess(UserAccess userAccess) {
        userAccessDao.saveUserAccess(userAccess);
    }

    @Override
    public void changeUserAccess(Long locationId, Long userId) {
        userAccessDao.changeUserAccess(locationId,userId);
    }

}
