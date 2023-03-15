package com.example.locationsystem.location;

import com.example.locationsystem.user.User;
import com.example.locationsystem.userAccess.UserAccess;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @NotBlank
    private String name;

    @NonNull
    @NotBlank
    private String address;

    @NonNull
    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "location")
    private List<UserAccess> accesses;

    public Location() {

    }
}

