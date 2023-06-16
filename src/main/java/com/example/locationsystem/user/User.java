package com.example.locationsystem.user;


import com.example.locationsystem.userAccess.UserAccess;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class User {

    private Long id;

    @NonNull
    private String username;

    @NonNull
    private String name;

    @NonNull
    private String password;

    private List<UserAccess> accesses;

    public User() {
    }

}