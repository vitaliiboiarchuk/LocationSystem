package com.example.locationsystem.location;

import com.example.locationsystem.user.User;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
public class Location {

    @NonNull
    private Long id;

    @NonNull
    private String name;

    @NonNull
    private String address;

    @NonNull
    private User user;

    public Location() {
    }
}

