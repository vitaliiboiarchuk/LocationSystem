package com.example.locationsystem.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {

    @Query("select u.readOnlyLocations from User u where u.id = ?1")
    List<Location> findAllMyReadOnlyLocations(Long id);

    @Query("select u.adminLocations from User u where u.id = ?1")
    List<Location> findAllMyAdminLocations(Long id);

    @Query("select l from Location l where l.user.id = ?1")
    List<Location> findAllMyLocations(Long id);

}
