package com.example.locationsystem.userAccess;

import com.example.locationsystem.event.EventService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAccessServiceImpl implements UserAccessService {

    UserAccessDao userAccessDao;
    EventService eventService;

    @Override
    public CompletableFuture<UserAccess> saveUserAccess(UserAccess userAccess) {

        log.info("Saving user access by location id={}, user id={}",
            userAccess.getLocationId(), userAccess.getUserId());
        return userAccessDao.saveUserAccess(userAccess)
            .thenApply(savedUserAccess -> {
                eventService.publishUserAccessCreatedEvent(savedUserAccess);
                return savedUserAccess;
            });
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

