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
    private String name;

    @NonNull
    private String address;

    @NonNull
    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "location",cascade = CascadeType.ALL)
    private List<UserAccess> accesses;

    public Location() {

    }
}

