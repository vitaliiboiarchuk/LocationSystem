package com.example.locationsystem.userAccess;

import com.example.locationsystem.event.ObjectChangeEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAccessServiceImpl implements UserAccessService {

    UserAccessDao userAccessDao;
    ApplicationEventPublisher eventPublisher;

    @Override
    public CompletableFuture<UserAccess> saveUserAccess(UserAccess userAccess) {

        log.info("Saving user access by location id={}, user id={}",
            userAccess.getLocationId(), userAccess.getUserId());
        return userAccessDao.saveUserAccess(userAccess)
            .thenApply(savedUserAccess -> {
                eventPublisher.publishEvent(new ObjectChangeEvent(this, ObjectChangeEvent.ObjectType.USER_ACCESS,
                    ObjectChangeEvent.ActionType.CREATED,
                    savedUserAccess, savedUserAccess.getId()));
                return savedUserAccess;
            });
    }

    @Override
    public CompletableFuture<UserAccess> findUserAccess(UserAccess userAccess, Long ownerId) {

        log.info("Finding user access by location id={}, user to share id={}, owner id={}",
            userAccess.getLocationId(), userAccess.getUserId(), ownerId);
        return userAccessDao.findUserAccess(userAccess, ownerId);
    }

    @Override
    public CompletableFuture<Void> changeUserAccess(UserAccess userAccess) {

        log.info("Changing user access by location id={}, user id={}",
            userAccess.getLocationId(), userAccess.getUserId());
        return userAccessDao.changeUserAccess(userAccess);
    }
}

