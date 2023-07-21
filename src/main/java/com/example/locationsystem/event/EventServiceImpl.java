package com.example.locationsystem.event;

import com.example.locationsystem.location.Location;
import com.example.locationsystem.user.User;
import com.example.locationsystem.userAccess.UserAccess;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {

    public static final String USER = "User";
    public static final String LOCATION = "Location";
    public static final String USER_ACCESS = "UserAccess";

    ApplicationEventPublisher eventPublisher;

    @Override
    public void publishUserCreatedEvent(User user) {

        eventPublisher.publishEvent(new ObjectChangeEvent(this, USER, ObjectChangeEvent.ActionType.CREATED, user));
    }

    @Override
    public void publishUserDeletedEvent(User user) {

        eventPublisher.publishEvent(new ObjectChangeEvent(this, USER, ObjectChangeEvent.ActionType.DELETED, user));
    }

    @Override
    public void publishLocationCreatedEvent(Location location) {

        eventPublisher.publishEvent(new ObjectChangeEvent(this, LOCATION, ObjectChangeEvent.ActionType.CREATED,
            location));
    }

    @Override
    public void publishLocationDeletedEvent(Location location) {

        eventPublisher.publishEvent(new ObjectChangeEvent(this, LOCATION, ObjectChangeEvent.ActionType.DELETED,
            location));
    }

    @Override
    public void publishUserAccessCreatedEvent(UserAccess userAccess) {

        eventPublisher.publishEvent(new ObjectChangeEvent(this, USER_ACCESS, ObjectChangeEvent.ActionType.CREATED,
            userAccess));
    }
}

