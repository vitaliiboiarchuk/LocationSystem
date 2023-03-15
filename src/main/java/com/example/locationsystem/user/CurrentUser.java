package com.example.locationsystem.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CurrentUser extends User {

    private final com.example.locationsystem.user.User user;
    public CurrentUser(String username, String password,
                       Collection<? extends GrantedAuthority> authorities,
                       com.example.locationsystem.user.User user) {
        super(username, password, authorities);
        this.user = user;
    }
    public com.example.locationsystem.user.User getUser() {return user;}

}
