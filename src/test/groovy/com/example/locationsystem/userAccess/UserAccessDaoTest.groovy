package com.example.locationsystem.userAccess

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.CompletableFuture

class UserAccessDaoTest extends Specification {

    @Subject
    UserAccessDao userAccessDao

    JdbcTemplate jdbcTemplate

    def setup() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource()
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver")
        dataSource.setUrl("jdbc:mysql://localhost:3306/task1")
        dataSource.setUsername("root")
        dataSource.setPassword("")

        jdbcTemplate = new JdbcTemplate(dataSource)
        userAccessDao = new UserAccessDao(jdbcTemplate)

        jdbcTemplate.execute("INSERT INTO users(id,username,name,password) VALUES(1,'user1','name1','pass1')")
        jdbcTemplate.execute("INSERT INTO users(id,username,name,password) VALUES(2,'user2','name2','pass2')")
        jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES(1,'name1','add1',1)")
        jdbcTemplate.execute("INSERT INTO accesses(id,title,location_id,user_id) VALUES(1,'ADMIN',1,2)")
    }

    def cleanup() {

        jdbcTemplate.execute("DELETE FROM accesses WHERE id = 1")
        jdbcTemplate.execute("DELETE FROM locations WHERE id = 1")
        jdbcTemplate.execute("DELETE FROM users WHERE id = 1")
        jdbcTemplate.execute("DELETE FROM users WHERE id = 2")
    }

    def "should save user access"() {

        given:
            def accessUserId = 6L
            def locationId = 5L
            jdbcTemplate.execute("INSERT INTO users(id,username,password,name) VALUES(5,'user55','pass55','name55'),(6,'user56','pass56','name56')")
            jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES(5,'loc56','add56',5)")

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
            jdbcTemplate.execute("DELETE FROM locations WHERE id = 5")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 5")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 6")
    }

    def "should find user access"() {

        given:
            def access = new UserAccess("ADMIN", 2, 1)

        when:
            CompletableFuture<UserAccess> result = userAccessDao.findUserAccess(access)

        then:
            UserAccess userAccess = result.get()
            userAccess.getUserId() == 2L
            userAccess.getLocationId() == 1L
            userAccess.getTitle() == 'ADMIN'
    }

    def "should change user access"() {

        given:
            def access = new UserAccess("ADMIN", 2, 1)

        when:
            CompletableFuture<Void> result = userAccessDao.changeUserAccess(access)

        then:
            result.get() == null
            String newTitle = jdbcTemplate.queryForObject("SELECT title FROM accesses WHERE location_id = ? AND user_id = ?", String.class, 1L, 2L)
            newTitle == "READ"
    }
}
