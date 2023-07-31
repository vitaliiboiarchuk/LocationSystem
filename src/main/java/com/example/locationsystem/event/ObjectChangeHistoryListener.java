package com.example.locationsystem.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ObjectChangeHistoryListener {

    EventDao eventDao;

    @EventListener(ObjectChangeEvent.class)
    public void insertEvent(ObjectChangeEvent event) {

        log.info("Event={} received", event);
        eventDao.insertEvent(event.getObjectType(), event.getActionType(), event.getData(), event.getObjectId());
    }
}

