package com.example.locationsystem.user;


import com.example.locationsystem.userAccess.UserAccess;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.util.List;

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

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<UserAccess> accesses;

    public User() {

    }
}

