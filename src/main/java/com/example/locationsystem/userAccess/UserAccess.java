package com.example.locationsystem.userAccess;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserAccess {

    private Long id;

    @NonNull
    private String title;

    @NonNull
    private Long userId;

    @NonNull
    private Long locationId;

    public UserAccess() {

    }
}

