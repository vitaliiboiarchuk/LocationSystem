package com.example.locationsystem.utils;

import org.springframework.stereotype.Component;

@Component
public class EmailUtils {

    public String hideEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex > 1) {
            String username = email.substring(0, atIndex);
            String domain = email.substring(atIndex + 1);
            int usernameLength = username.length();
            String maskedUsername = username.substring(0, 2);
            StringBuilder maskedUsernameBuilder = new StringBuilder(maskedUsername);
            for (int i = 2; i < usernameLength; i++) {
                maskedUsernameBuilder.append("*");
            }
            maskedUsername = maskedUsernameBuilder.toString();
            return maskedUsername + "@" + domain;
        }
        return email;
    }
}
