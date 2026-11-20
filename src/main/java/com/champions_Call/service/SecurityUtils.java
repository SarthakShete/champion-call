package com.champions_Call.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.champions_Call.model.User;

@Service
public class SecurityUtils {

    public User getLoggedInUser() {

        // If the user not found in the session then we take it spring security 
        // Then we again put the User into the session...
        // Check if a valid user is logged in

        // Get the logged-in user object
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }
}
