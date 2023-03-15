package com.example.locationsystem.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    Location findLocationByName(String name);

    List<Location> findLocationsByUserId(Long id);

    @Query("select l from Location l join UserAccess ua on l.id = ua.location.id where ua.user.id = ?1 and ua.title = ?2")
    List<Location> findAllAccessLocationsByUserIdAndTitle(Long id, String title);

}
