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

}
