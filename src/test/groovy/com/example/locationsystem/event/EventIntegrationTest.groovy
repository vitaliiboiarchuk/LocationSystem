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
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
class EventIntegrationTest extends Specification {

    @Shared
    def user = new User(username: "test@gmail.com", name: "test", password: "pass")

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

        when:
            def savedUserId = userService.saveUser(user).join()

        then:
            def exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM history WHERE object_id = ? AND object_type = ? AND action_type = ?)",
                Boolean,
                savedUserId,
                ObjectChangeEvent.ObjectType.USER.name(),
                ObjectChangeEvent.ActionType.CREATED.name()
            )
            exists

        cleanup:
            jdbcTemplate.execute(DELETE_USER)
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
    }

    def "should insert event into database when user is deleted"() {

        given:
            def savedUserId = userService.saveUser(user).join()

        when:
            userService.deleteUserByEmail(user.getUsername()).join()

        then:
            def exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM history WHERE object_id = ? AND object_type = ? AND action_type = ?)",
                Boolean,
                savedUserId,
                ObjectChangeEvent.ObjectType.USER.name(),
                ObjectChangeEvent.ActionType.DELETED.name()
            )
            exists

        cleanup:
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
    }

    def "should insert event into database when location is created"() {

        given:
            def savedUserId = userService.saveUser(user).join()

            def location = new Location(name: "test", address: "test", userId: savedUserId)

        when:
            def savedLoc = locationService.saveLocation(location, location.getUserId()).join()

        then:
            def exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM history WHERE object_id = ? AND object_type = ? AND action_type = ?)",
                Boolean,
                savedLoc.getId(),
                ObjectChangeEvent.ObjectType.LOCATION.name(),
                ObjectChangeEvent.ActionType.CREATED.name()
            )
            exists

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION)
            jdbcTemplate.execute(DELETE_USER)
            jdbcTemplate.update(DELETE_EVENT, savedLoc.getId())
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
    }

    def "should insert event into database when location is deleted"() {

        given:
            def savedUserId = userService.saveUser(user).join()

            def location = new Location(name: "test", address: "test", userId: savedUserId)
            def savedLoc = locationService.saveLocation(location, location.getUserId()).join()

        when:
            locationService.deleteLocation(savedLoc.getName(), savedLoc.getUserId()).join()

        then:
            def exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM history WHERE object_id = ? AND object_type = ? AND action_type = ?)",
                Boolean,
                savedLoc.getId(),
                ObjectChangeEvent.ObjectType.LOCATION.name(),
                ObjectChangeEvent.ActionType.DELETED.name()
            )
            exists

        cleanup:
            jdbcTemplate.execute(DELETE_USER)
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
            jdbcTemplate.update(DELETE_EVENT, savedLoc.getId())
    }

    def "should insert event into database when user access is created"() {

        given:
            def savedUserId = userService.saveUser(user).join()

            def location = new Location(name: "test", address: "test", userId: savedUserId)
            def savedLoc = locationService.saveLocation(location, location.getUserId()).join()

            def userAccess = new UserAccess(title: "test", userId: savedUserId, locationId: savedLoc.getId())

        when:
            def savedAccess = userAccessService.saveUserAccess(userAccess).join()

        then:
            def exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM history WHERE object_id = ? AND object_type = ? AND action_type = ?)",
                Boolean,
                savedAccess.getId(),
                ObjectChangeEvent.ObjectType.USER_ACCESS.name(),
                ObjectChangeEvent.ActionType.CREATED.name()
            )
            exists

        cleanup:
            jdbcTemplate.execute(DELETE_ACCESS)
            jdbcTemplate.execute(DELETE_LOCATION)
            jdbcTemplate.execute(DELETE_USER)
            jdbcTemplate.update(DELETE_EVENT, savedAccess.getId())
            jdbcTemplate.update(DELETE_EVENT, savedLoc.getId())
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
    }
}

