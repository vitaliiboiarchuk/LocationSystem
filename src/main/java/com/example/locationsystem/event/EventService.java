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
public class EventService {

    ApplicationEventPublisher eventPublisher;

    public void publishUserCreatedEvent(User user) {

        eventPublisher.publishEvent(new ObjectChangeEvent(this, ObjectChangeEvent.ObjectType.USER, ObjectChangeEvent.ActionType.CREATED, user));
    }

    public void publishUserDeletedEvent(User user) {

        eventPublisher.publishEvent(new ObjectChangeEvent(this, ObjectChangeEvent.ObjectType.USER, ObjectChangeEvent.ActionType.DELETED, user));
    }

    public void publishLocationCreatedEvent(Location location) {

        eventPublisher.publishEvent(new ObjectChangeEvent(this, ObjectChangeEvent.ObjectType.LOCATION, ObjectChangeEvent.ActionType.CREATED,
            location));
    }

    public void publishLocationDeletedEvent(Location location) {

        eventPublisher.publishEvent(new ObjectChangeEvent(this, ObjectChangeEvent.ObjectType.LOCATION, ObjectChangeEvent.ActionType.DELETED,
            location));
    }
    public void publishUserAccessCreatedEvent(UserAccess userAccess) {

        eventPublisher.publishEvent(new ObjectChangeEvent(this, ObjectChangeEvent.ObjectType.USER_ACCESS, ObjectChangeEvent.ActionType.CREATED,
            userAccess));
    }
}

