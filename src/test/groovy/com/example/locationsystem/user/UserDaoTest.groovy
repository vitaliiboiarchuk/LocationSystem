package com.example.locationsystem.user

import com.example.locationsystem.util.EmailUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

import javax.sql.DataSource
import java.util.concurrent.CompletableFuture

@SpringBootTest
class UserDaoTest extends Specification {

    UserDao userDao
    JdbcTemplate jdbcTemplate
    EmailUtil emailUtil

    @Autowired
    DataSource dataSource


    def setup() {

        jdbcTemplate = new JdbcTemplate(dataSource)
        emailUtil = new EmailUtil()
        userDao = new UserDao(jdbcTemplate, emailUtil)

        jdbcTemplate.execute("INSERT INTO users(id,name,username,password) VALUES(100,'name1','user1',SHA2('pass1',256))")
        jdbcTemplate.execute("INSERT INTO locations(id,address,name,user_id) VALUES(100,'Add1','name1',100)")
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
            jdbcTemplate.execute("INSERT INTO users(id,name,username,password) VALUES(200,'name2','user2',SHA2('pass2',256)),(300,'name3','user3',SHA2('pass3',256))")
            jdbcTemplate.execute("INSERT INTO accesses(id,title,location_id,user_id) VALUES(100,'ADMIN',100,300),(200,'ADMIN',100,200)")

        when:
            CompletableFuture<List<User>> futureResult = userDao.findAllUsersOnLocation(100, 300)

        then:
            List<User> users = futureResult.get()
            users[0].getUsername() == 'user2'

        cleanup:
            jdbcTemplate.execute("DELETE FROM accesses WHERE id = 100")
            jdbcTemplate.execute("DELETE FROM accesses WHERE id = 200")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 200")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 300")
    }

    def "should delete user by email"() {

        given:
            jdbcTemplate.execute("INSERT INTO users(id,name,username,password) VALUES(200,'name1','test@gmail.com',SHA2('pass1',256))")

        when:
            userDao.deleteUserByEmail("test@gmail.com")

        then:
            def deletedUser = jdbcTemplate.query("SELECT * FROM users WHERE username = ?", BeanPropertyRowMapper.newInstance(User.class), 200L)
            deletedUser.isEmpty()
    }

    def "should find user by id"() {

        when:
            CompletableFuture<User> futureResult = userDao.findUserById(100L)

        then:
            User user = futureResult.get()
            user.getUsername() == 'user1'
    }
}
