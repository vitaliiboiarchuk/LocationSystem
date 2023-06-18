package com.example.locationsystem.userAccess;

import com.example.locationsystem.location.Location;
import com.example.locationsystem.user.User;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
public class UserAccess {

    @NonNull
    private Long id;

    @NonNull
    private String title;

    @NonNull
    User user;

    @NonNull
    Location location;

    public UserAccess() {
    }
}

