package com.example.locationsystem.user;


import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
public class User {

    @NonNull
    private Long id;

    @NonNull
    private String username;

    @NonNull
    private String name;

    @NonNull
    private String password;

    public User() {
    }

}