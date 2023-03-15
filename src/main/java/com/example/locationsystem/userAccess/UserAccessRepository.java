package com.example.locationsystem.userAccess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccessRepository extends JpaRepository<UserAccess,Long> {

    UserAccess findUserAccessByUserId(Long id);
}
