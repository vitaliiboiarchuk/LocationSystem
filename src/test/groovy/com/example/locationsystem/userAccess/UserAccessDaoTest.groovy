package com.example.locationsystem.userAccess

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

import javax.sql.DataSource
import java.util.concurrent.CompletableFuture

@SpringBootTest
class UserAccessDaoTest extends Specification {

    UserAccessDao userAccessDao
    JdbcTemplate jdbcTemplate

    @Autowired
    DataSource dataSource

    def setup() {

        jdbcTemplate = new JdbcTemplate(dataSource)
        userAccessDao = new UserAccessDao(jdbcTemplate)

        jdbcTemplate.execute("INSERT INTO users(id, username, name, password) VALUES(100, 'user1', 'name1', 'pass1'), (200, 'user2', 'name2', 'pass2')")
        jdbcTemplate.execute("INSERT INTO locations(id, name, address, user_id) VALUES(100, 'name1', 'add1', 200)")
        jdbcTemplate.execute("INSERT INTO accesses(id, title, location_id, user_id) VALUES(100, 'ADMIN', 100, 100)")
    }

    def cleanup() {

        jdbcTemplate.execute("DELETE FROM accesses WHERE id = 100")
        jdbcTemplate.execute("DELETE FROM locations WHERE id = 100")
        jdbcTemplate.execute("DELETE FROM users WHERE id = 100")
        jdbcTemplate.execute("DELETE FROM users WHERE id = 200")
    }

    def "should save user access"() {

        given:
            def accessUserId = 600L
            def locationId = 500L
            jdbcTemplate.execute("INSERT INTO users(id,username,password,name) VALUES(500,'user55','pass55','name55'),(600,'user56','pass56','name56')")
            jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES(500,'loc56','add56',500)")

            UserAccess userAccess = new UserAccess("Title11", accessUserId, locationId)

        when:
            CompletableFuture<UserAccess> futureResult = userAccessDao.saveUserAccess(userAccess)

        then:
            UserAccess savedAccess = futureResult.get()
            savedAccess != null
            savedAccess.getTitle() == userAccess.getTitle()
            savedAccess.getLocationId() == userAccess.getLocationId()
            savedAccess.getUserId() == userAccess.getUserId()

        cleanup:
            jdbcTemplate.execute("DELETE FROM accesses WHERE title = 'Title11'")
            jdbcTemplate.execute("DELETE FROM locations WHERE id = 500")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 500")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 600")
    }

    def "should find user access"() {

        given:
            def access = new UserAccess("ADMIN", 100, 100)
        when:
            def result = userAccessDao.findUserAccess(access, 200)

        then:
            UserAccess userAccess = result.get()
            userAccess.getTitle() == 'ADMIN'
            userAccess.getLocationId() == 100
            userAccess.getUserId() == 100
    }

    def "should change user access"() {

        given:
            def access = new UserAccess("ADMIN", 100, 100)

        when:
            def result = userAccessDao.changeUserAccess(access, 200)

        then:
            UserAccess userAccess = result.get()
            userAccess.getUserId() == 100
            userAccess.getLocationId() == 100
            userAccess.getTitle() == "READ"
    }
}
