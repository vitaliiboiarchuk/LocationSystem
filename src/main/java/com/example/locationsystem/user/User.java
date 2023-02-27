package com.example.locationsystem.user;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String email;

    private String name;

    private String password;

    private int enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;


}

