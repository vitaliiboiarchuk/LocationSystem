package com.example.locationsystem.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEvent;

import java.sql.Timestamp;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ObjectChangeEvent extends ApplicationEvent {

    public enum ActionType {
        CREATED,
        DELETED
    }

    public enum ObjectType {
        USER,
        LOCATION,
        USER_ACCESS
    }

    ObjectType objectType;
    ActionType actionType;
    Timestamp eventTime;
    Long objectId;

    public ObjectChangeEvent(
        Object source,
        ObjectType objectType,
        ActionType actionType,
        Timestamp eventTime,
        Long objectId
    ) {

        super(source);
        this.objectType = objectType;
        this.actionType = actionType;
        this.eventTime = eventTime;
        this.objectId = objectId;
    }
}
