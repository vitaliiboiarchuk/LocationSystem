package com.example.locationsystem.userAccess;

import org.springframework.stereotype.Service;

@Service
public class UserAccessServiceImpl implements UserAccessService {

    private final UserAccessRepository userAccessRepository;

    public UserAccessServiceImpl(UserAccessRepository userAccessRepository) {
        this.userAccessRepository = userAccessRepository;
    }

    @Override
    public void saveUserAccess(UserAccess userAccess) {
        userAccessRepository.save(userAccess);
    }

    @Override
    public void changeUserAccess(Long locationId, Long userId) {
        UserAccess access = userAccessRepository.findUserAccessByLocationIdAndUserId(locationId, userId);
        if (access.getTitle().equals("READ")) {
            access.setTitle("ADMIN");
        } else if (access.getTitle().equals("ADMIN")) {
            access.setTitle("READ");
        }
        userAccessRepository.save(access);
    }

}
