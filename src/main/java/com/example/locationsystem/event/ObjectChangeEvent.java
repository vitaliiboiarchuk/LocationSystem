package com.example.locationsystem.event;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEvent;

@Data
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

    public ObjectChangeEvent(Object source, ObjectType objectType, ActionType actionType, Object data) {

        super(source);
        this.objectType = objectType;
        this.actionType = actionType;
        this.data = data;
    }
}
