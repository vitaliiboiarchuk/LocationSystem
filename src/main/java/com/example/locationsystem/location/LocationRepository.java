package com.example.locationsystem.location;

import com.example.locationsystem.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {


    @Query("select l from Location l where l.user.id = ?1 order by l.name asc")
    List<Location> findAllMyLocations(Long id);
}
