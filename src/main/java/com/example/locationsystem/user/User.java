package com.example.locationsystem.user;


import com.example.locationsystem.role.Role;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String username;

    @NonNull
    private String name;

    @NonNull
    private String password;

    @NonNull
    private int enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;

    public User() {

    }
}

