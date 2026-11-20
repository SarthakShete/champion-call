package com.champions_Call.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.champions_Call.model.User;
import com.champions_Call.service.SecurityUtils;
import com.champions_Call.service.UserServices;


@ControllerAdvice // marks a class that applies shared behavior to all controllers in your app, so you don’t repeat the same code in each one.
public class GlobalModelAttributes {

    // it is what puts currentUser into the model of every page. SecurityUtils only finds the user, it doesn’t pass it to Thymeleaf.

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private UserServices userServices;

    @ModelAttribute
    public void addCurrentUser(Model model) {

        System.out.println("The navbar global is called...");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return;
        }

        User user = securityUtils.getLoggedInUser();
        if (user == null) {
            // fallback for cases where the principal is not your User class
            user = userServices.getUserByEmail(auth.getName());
        }
        if (user == null) return;

        boolean isOrganizer = auth
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority()
                .equals("ROLE_ORGANIZER"));

        String photo = null;

        if (isOrganizer && user.getOrganizerProfile() != null) {
            photo = user.getOrganizerProfile().getDisplayProfilePicture();
        } else if (user.getPlayerProfile() != null) {
            photo = user.getPlayerProfile().getProfileImage();
        }

        // Last resort: the photo stored on User (Google login)
        if (photo == null || photo.isBlank()) {
            System.out.println("Photo is Null");
            photo = user.getProfilePic();
        }
        System.out.println("The Photo of global is :" +photo);
        model.addAttribute("navPhoto", photo);
        model.addAttribute("currentUser", user);
        model.addAttribute("dashboardUrl",
                isOrganizer ? "/api/organizer/dashboard" : "/api/player/profile");
    }
}
