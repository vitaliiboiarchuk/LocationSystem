package com.example.locationsystem.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventDao {

    private static final String INSERT_EVENT = "INSERT INTO history (object_type, action_type, event_time, object_id) VALUES (?, ?, ?, ?)";

    JdbcTemplate jdbcTemplate;

    public void insertEvent(
        ObjectChangeEvent.ObjectType objectType,
        ObjectChangeEvent.ActionType actionType,
        Timestamp eventTime,
        Long objectId
    ) {

        jdbcTemplate.update(INSERT_EVENT, objectType.name(), actionType.name(), eventTime, objectId);
        log.info("Event with object type={}, action type={}, object id={} inserted", objectType.name(), actionType.name(),
            objectId);
    }

}
