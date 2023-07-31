package com.example.locationsystem.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEvent;

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
    Object data;
    Long objectId;

    public ObjectChangeEvent(
        Object source,
        ObjectType objectType,
        ActionType actionType,
        Object data,
        Long objectId
    ) {

        super(source);
        this.objectType = objectType;
        this.actionType = actionType;
        this.data = data;
        this.objectId = objectId;
    }
}
