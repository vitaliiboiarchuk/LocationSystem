package com.example.locationsystem.event;

import com.example.locationsystem.location.Location;
import com.example.locationsystem.user.User;
import com.example.locationsystem.userAccess.UserAccess;

public interface EventService {

    void publishUserCreatedEvent(User user);

    void publishUserDeletedEvent(User user);

    void publishLocationCreatedEvent(Location location);

    void publishLocationDeletedEvent(Location location);

    void publishUserAccessCreatedEvent(UserAccess userAccess);
}
