package com.example.locationsystem.user;


import com.example.locationsystem.location.Location;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;


@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    private String name;

    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Location> readOnlyLocations;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Location> adminLocations;

    private int enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;


}

