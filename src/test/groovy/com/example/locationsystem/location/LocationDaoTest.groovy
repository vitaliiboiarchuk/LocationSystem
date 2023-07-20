package com.example.locationsystem.location

import com.example.locationsystem.userAccess.UserAccess
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification
import spock.lang.Subject

import javax.sql.DataSource
import java.util.concurrent.CompletableFuture

@SpringBootTest
class LocationDaoTest extends Specification {

    @Subject
    LocationDao locationDao

    JdbcTemplate jdbcTemplate

    @Autowired
    DataSource dataSource

    def setup() {

        jdbcTemplate = new JdbcTemplate(dataSource)
        locationDao = new LocationDao(jdbcTemplate)

        jdbcTemplate.execute("INSERT INTO users(id,name,password,username) VALUES(1,'vitalii','pass1','vitalii'),(2,'natalia','pass2','natalia'),(3,'oleh','pass3','oleh')")
        jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES (1, 'home','test',1),(2,'gym','naleczowska',1),(3,'swimming pool','sobieskiego',3)")
        jdbcTemplate.execute("INSERT INTO accesses(id,title,location_id,user_id) VALUES(1,'ADMIN',1,2),(2,'READ',3,1)")
    }

    def cleanup() {

        jdbcTemplate.execute("DELETE FROM accesses WHERE id = 1")
        jdbcTemplate.execute("DELETE FROM accesses WHERE id = 2")
        jdbcTemplate.execute("DELETE FROM locations WHERE id = 1")
        jdbcTemplate.execute("DELETE FROM locations WHERE id = 2")
        jdbcTemplate.execute("DELETE FROM locations WHERE id = 3")
        jdbcTemplate.execute("DELETE FROM users WHERE id = 1")
        jdbcTemplate.execute("DELETE FROM users WHERE id = 2")
        jdbcTemplate.execute("DELETE FROM users WHERE id = 3")
    }

    def "should find all user locations"() {

        when:
            CompletableFuture<List<Location>> futureResult = locationDao.findAllUserLocations(1L)

        then:
            List<Location> locations = futureResult.get()
            locations.size() == 3
            locations[0].getName() == 'swimming pool'
            locations[1].getName() == 'home'
            locations[2].getName() == 'gym'
    }

    def "should find location in user locations"() {

        when:
            CompletableFuture<Location> futureResult = locationDao.findLocationInUserLocations(1, 1)

        then:
            Location location = futureResult.get()
            location.getName() == 'home'
            location.getAddress() == 'test'
    }

    def "should find location by name and userId"() {

        when:
            CompletableFuture<Optional<Location>> futureResult = locationDao.findLocationByNameAndUserId("gym", 1L)

        then:
            futureResult.get().isPresent()
            futureResult.get().get().getAddress() == 'naleczowska'
    }

    def "should save location"() {

        given:
            Location location = new Location("title1", "add1", 1)

        when:
            CompletableFuture<Location> futureResult = locationDao.saveLocation(location)

        then:
            Location loc = futureResult.get()
            loc != null
            loc.getName() == 'title1'

        cleanup:
            jdbcTemplate.execute("DELETE FROM locations WHERE name = 'title1'")
    }

    def "should delete location"() {

        given:
            jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES(5,'name1','add1',1)")
            jdbcTemplate.execute("INSERT INTO accesses(id,title,location_id,user_id) VALUES(5,'ADMIN',5,2)")

        when:
            CompletableFuture<Void> futureResult = locationDao.deleteLocation('name1', 1L)

        then:
            futureResult.get() == null

        and:
            def deletedAccess = jdbcTemplate.query("SELECT * FROM accesses WHERE location_id = ?", BeanPropertyRowMapper.newInstance(UserAccess.class), 5L)
            deletedAccess.isEmpty()
            def deletedLocation = jdbcTemplate.query("SELECT * FROM locations WHERE id = ? AND user_id = ?", BeanPropertyRowMapper.newInstance(Location.class), 5L, 1L)
            deletedLocation.isEmpty()
    }

    def "should find locations not shared to user"() {

        when:
            CompletableFuture<Location> futureResult = locationDao.findNotSharedToUserLocation(1L, 2L, 2L)

        then:
            Location locToShare = futureResult.get()
            locToShare.getName() == 'gym'
    }

    def "should find location by id"() {

        when:
            CompletableFuture<Location> futureResult = locationDao.findLocationById(1L)

        then:
            Location loc = futureResult.get()
            loc.getName() == 'home'
    }
}
