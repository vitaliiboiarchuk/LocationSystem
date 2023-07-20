package com.example.locationsystem.user

import com.example.locationsystem.utils.EmailUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification
import spock.lang.Subject

import javax.sql.DataSource
import java.util.concurrent.CompletableFuture

@SpringBootTest
class UserDaoTest extends Specification {

    @Subject
    UserDao userDao

    JdbcTemplate jdbcTemplate

    @Autowired
    DataSource dataSource

    EmailUtils emailUtils

    def setup() {

        jdbcTemplate = new JdbcTemplate(dataSource)
        emailUtils = new EmailUtils()
        userDao = new UserDao(jdbcTemplate, emailUtils)

        jdbcTemplate.execute("INSERT INTO users(id,name,username,password) VALUES(1,'name1','user1',SHA2('pass1',256))")
        jdbcTemplate.execute("INSERT INTO locations(id,address,name,user_id) VALUES(1,'Add1','name1',1)")
    }

    def cleanup() {

        jdbcTemplate.execute("DELETE FROM locations WHERE name = 'name1'")
        jdbcTemplate.execute("DELETE FROM users WHERE name = 'name1'")
    }

    def "should find user by email"() {

        when:
            CompletableFuture<Optional<User>> futureResult = userDao.findUserByEmail("user1")

        then:
            futureResult.get().isPresent()
            futureResult.get().get().getUsername() == 'user1'
    }

    def "should find user by email and password"() {

        when:
            CompletableFuture<User> futureResult = userDao.findUserByEmailAndPassword("user1", "pass1")

        then:
            User user = futureResult.get()
            user.getUsername() == 'user1'
    }

    def "should save user"() {

        given:
            User user = new User("user4", "name4", "pass4")

        when:
            CompletableFuture<User> futureResult = userDao.saveUser(user)

        then:

            User savedUser = futureResult.get()
            savedUser.getUsername() == user.getUsername()

        cleanup:
            jdbcTemplate.execute("DELETE FROM users WHERE username = 'user4'")
    }

    def "should find all users with access on location"() {

        given:
            jdbcTemplate.execute("INSERT INTO users(id,name,username,password) VALUES(2,'name2','user2',SHA2('pass2',256)),(3,'name3','user3',SHA2('pass3',256))")
            jdbcTemplate.execute("INSERT INTO accesses(id,title,location_id,user_id) VALUES(1,'ADMIN',1,3),(2,'ADMIN',1,2)")

        when:
            CompletableFuture<List<User>> futureResult = userDao.findAllUsersOnLocation(1, 3)

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
            CompletableFuture<User> futureResult = userDao.findLocationOwner('name1',1L)

        then:
            User owner = futureResult.get()
            owner.getName() == 'name1'
    }

    def "should delete user by email"() {

        given:
            jdbcTemplate.execute("INSERT INTO users(id,name,username,password) VALUES(2,'name1','test@gmail.com',SHA2('pass1',256))")

        when:
            CompletableFuture<Void> futureResult = userDao.deleteUserByEmail("test@gmail.com")

        then:
            futureResult.get() == null

        and:
            def deletedUser = jdbcTemplate.query("SELECT * FROM users WHERE username = ?", BeanPropertyRowMapper.newInstance(User.class), 2L)
            deletedUser.isEmpty()
    }

    def "should find user by id"() {

        when:
            CompletableFuture<User> futureResult = userDao.findUserById(1L)

        then:
            User user = futureResult.get()
            user.getUsername() == 'user1'
    }
}
