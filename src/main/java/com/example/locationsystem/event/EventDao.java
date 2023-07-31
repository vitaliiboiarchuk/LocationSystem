package com.example.locationsystem.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventDao {

    private static final String INSERT_EVENT = "INSERT INTO history (object_type, action_type, details, object_id) VALUES (?, ?, ?, ?)";

    JdbcTemplate jdbcTemplate;

    public void insertEvent(
        ObjectChangeEvent.ObjectType objectType,
        ObjectChangeEvent.ActionType actionType,
        Object data,
        Long objectId
    ) {

        jdbcTemplate.update(INSERT_EVENT, objectType.name(), actionType.name(), data.toString(), objectId);
        log.info("Event with object type={}, action type={}, data={} inserted", objectType.name(), actionType.name(),
            data.toString());
    }

}
