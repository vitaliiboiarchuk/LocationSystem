package com.example.locationsystem.location;

import com.example.locationsystem.user.User;
import com.example.locationsystem.userAccess.UserAccess;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class Location {

    private Long id;

    @NonNull
    private String name;

    @NonNull
    private String address;

    @NonNull
    private User user;

    private List<UserAccess> accesses;

    public Location() {
    }
}

