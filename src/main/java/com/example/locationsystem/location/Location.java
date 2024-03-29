package com.example.locationsystem.location;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@RequiredArgsConstructor
public class Location {

    private Long id;

    @NonNull
    @NotBlank
    private String name;

    @NonNull
    @NotBlank
    private String address;

    @NonNull
    private Long userId;

    public Location() {

    }
}

