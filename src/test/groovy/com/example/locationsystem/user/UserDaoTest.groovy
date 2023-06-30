package com.example.locationsystem.user

import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.CompletableFuture

class UserDaoTest extends Specification {

    @Subject
    UserDao userDao

    JdbcTemplate jdbcTemplate

    def setup() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource()
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver")
        dataSource.setUrl("jdbc:mysql://localhost:3306/task1")
        dataSource.setUsername("root")
        dataSource.setPassword("")

        jdbcTemplate = new JdbcTemplate(dataSource)
        userDao = new UserDao(jdbcTemplate)

        jdbcTemplate.execute("INSERT INTO users(id,name,username,password) VALUES(1,'name1','user1',SHA2('pass1',256))")
        jdbcTemplate.execute("INSERT INTO locations(id,address,name,user_id) VALUES(1,'Add1','Name1',1)")
    }

    def cleanup() {

        jdbcTemplate.execute("DELETE FROM locations WHERE id = 1")
        jdbcTemplate.execute("DELETE FROM users WHERE id = 1")
    }

    def "should find user by username"() {

        when:
            CompletableFuture<User> futureResult = userDao.findByUsername("user1")

        then:
            User user = futureResult.get()
            user.getUsername() == 'user1'
    }

    def "should find user by username and password"() {

        when:
            CompletableFuture<User> futureResult = userDao.findUserByUsernameAndPassword("user1", "pass1")

        then:
            User user = futureResult.get()
            user.getUsername() == 'user1'
    }

    def "should save user"() {

        given:
            User user = new User("user4", "name4", "pass4")

        when:
            CompletableFuture<Void> futureResult = userDao.saveUser(user)

        then:
            futureResult.get() == null

        and:
            def savedUser = jdbcTemplate.queryForObject("SELECT * FROM users WHERE username = ?", BeanPropertyRowMapper.newInstance(User.class), user.getUsername())
            savedUser != null
            savedUser.getName() == user.getName()
            savedUser.getPassword() != user.getPassword()

        cleanup:
            jdbcTemplate.execute("DELETE FROM users WHERE username = 'user4'")
    }

    def "should find user by id"() {

        when:
            CompletableFuture<User> futureResult = userDao.findById(1L)

        then:
            User user = futureResult.get()
            user.getUsername() == 'user1'
    }

    def "should find all users with access on location"() {

        given:
            jdbcTemplate.execute("INSERT INTO users(id,name,username,password) VALUES(2,'name2','user2',SHA2('pass2',256)),(3,'name3','user3',SHA2('pass3',256))")
            jdbcTemplate.execute("INSERT INTO accesses(id,title,location_id,user_id) VALUES(1,'ADMIN',1,3),(2,'ADMIN',1,2)")

        when:
            CompletableFuture<List<User>> futureResult = userDao.findAllUsersWithAccessOnLocation(1, "ADMIN", 3)

        then:
            List<User> users = futureResult.get()
            users[0].getUsername() == 'user2'

        cleanup:
            jdbcTemplate.execute("DELETE FROM accesses WHERE id = 1")
            jdbcTemplate.execute("DELETE FROM accesses WHERE id = 2")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 2")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 3")
    }

    def "should find location owner"() {

        when:
            CompletableFuture<User> futureResult = userDao.findLocationOwner(1L)

        then:
            User owner = futureResult.get()
            owner.getName() == 'name1'
    }

    def "should delete user by username"() {

        given:
            jdbcTemplate.execute("INSERT INTO users(id,name,username,password) VALUES(100,'name1','test@gmail.com',SHA2('pass1',256))")

        when:
            CompletableFuture<Void> futureResult = userDao.deleteUserByUsername("test@gmail.com")

        then:
            futureResult.get() == null

        and:

            def deletedUser = jdbcTemplate.query("SELECT * FROM users WHERE username = ?", BeanPropertyRowMapper.newInstance(User.class), 100L)
            deletedUser.isEmpty()
    }
}
