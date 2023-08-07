package com.example.locationsystem.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ObjectChangeHistoryListener {

    EventDao eventDao;

    @EventListener(ObjectChangeEvent.class)
    public void insertEvent(ObjectChangeEvent event) {

        eventDao.insertEvent(event.getObjectType(), event.getActionType(), event.getEventTime(), event.getObjectId());
    }
}

