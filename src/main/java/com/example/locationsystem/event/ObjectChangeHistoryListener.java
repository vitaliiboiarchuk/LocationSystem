package com.example.locationsystem.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ObjectChangeHistoryListener implements ApplicationListener<ObjectChangeEvent> {

    JdbcTemplate jdbcTemplate;

    @Override
    public void onApplicationEvent(ObjectChangeEvent event) {

        ObjectChangeEvent.ObjectType objectType = event.getObjectType();
        ObjectChangeEvent.ActionType actionType = event.getActionType();
        Object data = event.getData();

        String sql = "INSERT INTO history (object_type, action_type, details) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, objectType.name(), actionType.name(), data.toString());
    }
}

