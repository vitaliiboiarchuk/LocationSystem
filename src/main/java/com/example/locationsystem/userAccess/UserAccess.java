package com.example.locationsystem.userAccess;

import com.example.locationsystem.location.Location;
import com.example.locationsystem.user.User;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "accesses")
public class UserAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String title;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "location_id")
    Location location;

    public UserAccess() {

    }
}

