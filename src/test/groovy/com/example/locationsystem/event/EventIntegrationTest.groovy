package com.example.locationsystem.event

import com.example.locationsystem.location.Location
import com.example.locationsystem.location.LocationService
import com.example.locationsystem.user.User
import com.example.locationsystem.user.UserService
import com.example.locationsystem.userAccess.UserAccess
import com.example.locationsystem.userAccess.UserAccessService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

@SpringBootTest
class EventIntegrationTest extends Specification {

    @Autowired
    UserService userService

    @Autowired
    LocationService locationService

    @Autowired
    UserAccessService userAccessService

    @Autowired
    JdbcTemplate jdbcTemplate

    private static final String DELETE_USER = "DELETE FROM users WHERE username = 'test@gmail.com';"
    private static final String DELETE_LOCATION = "DELETE FROM locations WHERE name = 'test';"
    private static final String DELETE_ACCESS = "DELETE FROM accesses WHERE title = 'test';"
    private static final String DELETE_EVENT = "DELETE FROM history WHERE object_id = ?;"

    def "should insert event into database when user is created"() {

        given:
            def user = new User(username: "test@gmail.com", name: "test", password: "pass")

        when:
            def savedUser = userService.saveUser(user).join()

        then:
            def exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM history WHERE object_id = ? AND object_type = ? AND action_type = ? AND details = ?)",
                Boolean,
                savedUser.getId(),
                ObjectChangeEvent.ObjectType.USER.name(),
                ObjectChangeEvent.ActionType.CREATED.name(),
                savedUser.toString()
            )
            exists

        cleanup:
            jdbcTemplate.execute(DELETE_USER)
            jdbcTemplate.update(DELETE_EVENT, savedUser.getId())
    }

    def "should insert event into database when user is deleted"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            def savedUser = userService.saveUser(user)
                .thenCompose({ result -> userService.findUserById(user.getId()) }).join()

        when:
            userService.deleteUserByEmail(savedUser.getUsername()).join()

        then:
            def exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM history WHERE object_id = ? AND object_type = ? AND action_type = ? AND details = ?)",
                Boolean,
                savedUser.getId(),
                ObjectChangeEvent.ObjectType.USER.name(),
                ObjectChangeEvent.ActionType.DELETED.name(),
                savedUser.toString()
            )
            exists

        cleanup:
            jdbcTemplate.update(DELETE_EVENT, savedUser.getId())
    }

    def "should insert event into database when location is created"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            def savedUser = userService.saveUser(user).join()

            def location = new Location(name: "test", address: "test", userId: savedUser.getId())

        when:
            def savedLoc = locationService.saveLocation(location, location.getUserId()).join()

        then:
            def exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM history WHERE object_id = ? AND object_type = ? AND action_type = ? AND details = ?)",
                Boolean,
                savedLoc.getId(),
                ObjectChangeEvent.ObjectType.LOCATION.name(),
                ObjectChangeEvent.ActionType.CREATED.name(),
                savedLoc.toString()
            )
            exists

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION)
            jdbcTemplate.execute(DELETE_USER)
            jdbcTemplate.update(DELETE_EVENT, savedLoc.getId())
            jdbcTemplate.update(DELETE_EVENT, savedUser.getId())
    }

    def "should insert event into database when location is deleted"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            def savedUser = userService.saveUser(user).join()

            def location = new Location(name: "test", address: "test", userId: savedUser.getId())
            def savedLoc = locationService.saveLocation(location, location.getUserId()).join()

        when:
            locationService.deleteLocation(savedLoc.getName(), savedLoc.getUserId()).join()

        then:
            def exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM history WHERE object_id = ? AND object_type = ? AND action_type = ? AND details = ?)",
                Boolean,
                savedLoc.getId(),
                ObjectChangeEvent.ObjectType.LOCATION.name(),
                ObjectChangeEvent.ActionType.DELETED.name(),
                savedLoc.toString()
            )
            exists

        cleanup:
            jdbcTemplate.execute(DELETE_USER)
            jdbcTemplate.update(DELETE_EVENT, savedUser.getId())
            jdbcTemplate.update(DELETE_EVENT, savedLoc.getId())
    }

    def "should insert event into database when user access is created"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            def savedUser = userService.saveUser(user).join()

            def location = new Location(name: "test", address: "test", userId: savedUser.getId())
            def savedLoc = locationService.saveLocation(location, location.getUserId()).join()

            def userAccess = new UserAccess(title: "test", userId: savedUser.getId(), locationId: savedLoc.getId())

        when:
            def savedAccess = userAccessService.saveUserAccess(userAccess).join()

        then:
            def exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM history WHERE object_id = ? AND object_type = ? AND action_type = ? AND details = ?)",
                Boolean,
                savedAccess.getId(),
                ObjectChangeEvent.ObjectType.USER_ACCESS.name(),
                ObjectChangeEvent.ActionType.CREATED.name(),
                savedAccess.toString()
            )
            exists

        cleanup:
            jdbcTemplate.execute(DELETE_ACCESS)
            jdbcTemplate.execute(DELETE_LOCATION)
            jdbcTemplate.execute(DELETE_USER)
            jdbcTemplate.update(DELETE_EVENT, savedAccess.getId())
            jdbcTemplate.update(DELETE_EVENT, savedLoc.getId())
            jdbcTemplate.update(DELETE_EVENT, savedUser.getId())
    }
}

