package com.example.locationsystem.location

import com.example.locationsystem.user.User
import com.example.locationsystem.userAccess.UserAccess
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.CompletableFuture

class LocationDaoTest extends Specification {

    @Subject
    LocationDao locationDao

    JdbcTemplate jdbcTemplate

    def setup() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource()
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver")
        dataSource.setUrl("jdbc:mysql://localhost:3306/task1")
        dataSource.setUsername("root")
        dataSource.setPassword("")

        jdbcTemplate = new JdbcTemplate(dataSource)
        locationDao = new LocationDao(jdbcTemplate)
    }

    def "should find all added locations"() {

        given:
            jdbcTemplate.execute("INSERT INTO users(id,name,password,username) VALUES(1,'name1','pass1','user1')")
            jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES(1,'name1','add1',1),(2,'name2','add2',1)")

        when:
            CompletableFuture<List<Location>> futureResult = locationDao.findAllAddedLocations(1L)

        then:
            List<Location> locations = futureResult.get()
            locations.size() == 2
            locations[0].getName() == 'name1'
            locations[1].getName() == 'name2'

        cleanup:
            jdbcTemplate.execute("DELETE FROM locations WHERE id = 1")
            jdbcTemplate.execute("DELETE FROM locations WHERE id = 2")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 1")
    }

    def "should find all locations with access"() {

        given:
            jdbcTemplate.execute("INSERT INTO users(id,name,password,username) VALUES(1,'name1','pass1','user1'),(2,'name2','pass2','user2')")
            jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES(1,'name1','add1',1)")
            jdbcTemplate.execute("INSERT INTO accesses(id,title,location_id,user_id) VALUES(1,'ADMIN',1,2)")

        when:
            CompletableFuture<List<Location>> futureResult = locationDao.findAllLocationsWithAccess(2, 'ADMIN')

        then:
            List<Location> locations = futureResult.get()
            locations.size() == 1
            locations[0].getName() == 'name1'

        cleanup:
            jdbcTemplate.execute("DELETE FROM accesses WHERE id = 1")
            jdbcTemplate.execute("DELETE FROM locations WHERE id = 1")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 2")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 1")
    }

    def "should find location by name and userId"() {

        given:
            jdbcTemplate.execute("INSERT INTO users(id,name,password,username) VALUES(1,'name1','pass1','user1')")
            jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES(1,'name1','add1',1)")

        when:
            CompletableFuture<Location> futureResult = locationDao.findLocationByNameAndUserId("name1", 1L)

        then:
            Location location = futureResult.get()
            location.getAddress() == 'add1'

        cleanup:
            jdbcTemplate.execute("DELETE FROM locations WHERE id = 1")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 1")
    }

    def "should save location"() {

        given:
            User user = new User(1L, "user1", "name1", "pass1")
            jdbcTemplate.execute("INSERT INTO users(id,name,password,username) VALUES(1,'name1','pass1','user1')")
            Location location = new Location(1L, "title1", "add1", user)

        when:
            CompletableFuture<Void> futureResult = locationDao.saveLocation(location)

        then:
            futureResult.get() == null

        and:
            def savedLocation = jdbcTemplate.queryForObject("SELECT * FROM locations WHERE name = ?", BeanPropertyRowMapper.newInstance(Location.class), location.getName())
            savedLocation != null
            savedLocation.getName() == 'title1'

        cleanup:
            jdbcTemplate.execute("DELETE FROM locations WHERE name = 'title1'")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 1")
    }

    def "should delete location"() {

        given:
            jdbcTemplate.execute("INSERT INTO users(id,name,password,username) VALUES(1,'name1','pass1','user1'),(2,'name2','pass2','user2')")
            jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES(1,'name1','add1',1)")
            jdbcTemplate.execute("INSERT INTO accesses(id,title,location_id,user_id) VALUES(1,'ADMIN',1,2)")

        when:
            CompletableFuture<Void> futureResult = locationDao.deleteLocation(1L, 1L)

        then:
            futureResult.get() == null

        and:
            def deletedAccess = jdbcTemplate.query("SELECT * FROM accesses WHERE location_id = ?", BeanPropertyRowMapper.newInstance(UserAccess.class), 1L)
            deletedAccess.isEmpty()
            def deletedLocation = jdbcTemplate.query("SELECT * FROM locations WHERE id = ? AND user_id = ?", BeanPropertyRowMapper.newInstance(Location.class), 1L, 1L)
            deletedLocation.isEmpty()

        cleanup:
            jdbcTemplate.execute("DELETE FROM users WHERE id = 1")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 2")
    }

    def "should find locations not shared to user"() {

        given:
            jdbcTemplate.execute("INSERT INTO users(id,name,password,username) VALUES(1,'vitalii','pass1','vitalii'),(2,'natalia','pass2','natalia'),(3,'oleh','pass3','oleh')")
            jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES (1, 'home','krolowej marysienki',1),(2,'gym','naleczowska',1),(3,'swimming pool','sobieskiego',3)")
            jdbcTemplate.execute("INSERT INTO accesses(id,title,location_id,user_id) VALUES(1,'ADMIN',1,2),(2,'ADMIN',3,1)")

        when:
            CompletableFuture<List<Location>> futureResult = locationDao.findNotSharedToUserLocations(1L, 2L)

        then:
            List<Location> locsToShare = futureResult.get()
            locsToShare.size() == 2
            locsToShare[0].getName() == 'gym'
            locsToShare[1].getName() == 'swimming pool'

        cleanup:
            jdbcTemplate.execute("DELETE FROM accesses WHERE id = 1")
            jdbcTemplate.execute("DELETE FROM accesses WHERE id = 2")
            jdbcTemplate.execute("DELETE FROM locations WHERE id = 1")
            jdbcTemplate.execute("DELETE FROM locations WHERE id = 2")
            jdbcTemplate.execute("DELETE FROM locations WHERE id = 3")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 1")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 2")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 3")
    }

    def "should find location by id"() {

        given:
            jdbcTemplate.execute("INSERT INTO users(id,name,password,username) VALUES(1,'vitalii','pass1','vitalii')")
            jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES (1, 'home','krolowej marysienki',1)")


        when:
            CompletableFuture<Location> futureResult = locationDao.findById(1L)

        then:
        Location loc = futureResult.get()
        loc.getName() == 'home'

        cleanup:
            jdbcTemplate.execute("DELETE FROM locations WHERE id = 1")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 1")
    }
}
