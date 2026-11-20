package com.champions_Call.Configuration;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;
import com.champions_Call.Helper.AppConstants;
import com.champions_Call.model.Provider;
import com.champions_Call.model.User;
import com.champions_Call.repository.UserRepo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class oauthAuthenticationSuccessHandler implements AuthenticationSuccessHandler{

     @Autowired
    private UserRepo userRepo;

    @Autowired
    @Lazy
    private PersistentTokenRepository tokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication authentication) throws IOException, ServletException {

     
        
       Logger logger = LoggerFactory.getLogger(oauthAuthenticationSuccessHandler.class);

       logger.info("oauthAuthenticationSuccessHandler");

       // Firstly you should have to identify the provider then according to that use attributes...

       // Identify the token 

       var oauth2AuthenticationToken = (OAuth2AuthenticationToken)authentication;

       // This is give exact Provider either it's google or Github...
       String authorizedClientRegistrationId = oauth2AuthenticationToken.getAuthorizedClientRegistrationId();

       logger.info(authorizedClientRegistrationId);

       
       var oauthUser = (DefaultOAuth2User)authentication.getPrincipal();

       // Using this so we can understand which attribute we get....
       // because attribute change according to the Provider....
       oauthUser.getAttributes().forEach((key,value) -> {
            logger.info(key + " : "+value);
       });
       

       // Default value that we have to set for both provider even if provider is Google or Github...   

       String assignedRole = AppConstants.ROLE_PLAYER;

       Cookie cookies[] = req.getCookies();
        if(cookies != null){
            // Browser will send all cookies so we have to each one
            // our cookie is role..
            for(Cookie cookie : cookies){
                if("Role".equals(cookie.getName())){

                    String cookieValue = cookie.getValue().toUpperCase();
            
                    // 3. STRICT SECURITY CHECK: Only upgrade them if it exactly matches "ORGANIZER"
                    if (cookieValue.equals(AppConstants.ROLE_ORGANIZER)) {
                        assignedRole = AppConstants.ROLE_ORGANIZER;
                        logger.info("Found Organizer cookie! Upgrading OAuth user.");
                    }
                    break; // We found our cookie, stop looping
                }
            }
        }
        

       User user = new User();
       user.setRole(assignedRole);
       user.setEmailVerified(true);
       

       
       if(authorizedClientRegistrationId.equalsIgnoreCase("google")){
          
            // 1. Check for null before calling toString() to prevent the crash
            Object emailObj = oauthUser.getAttribute("email");
            Object firstNameObj = oauthUser.getAttribute("given_name"); // Use "given_name" from your logs
            Object lastNameObj = oauthUser.getAttribute("family_name");
            
            // 2. Safely assign values
            if (emailObj != null) user.setEmail(emailObj.toString());
            if (firstNameObj != null) user.setFirstName(firstNameObj.toString());

            // FIX: Map family_name to user_last (lastName)
            if (lastNameObj != null) {
                user.setLastName(lastNameObj.toString());
            } else {
                // Fallback in case Google doesn't provide a last name
                user.setLastName("Unknown"); 
            }
            
            user.setProviderUserId(oauthUser.getName());
            user.setProvider(Provider.GOOGLE);
       }
       

       String email = user.getEmail();
       User userdb = userRepo.findByEmail(email).orElse(null);

       // Determine role: existing DB role wins; otherwise check the "Role" cookie
        String assignedRole1 = AppConstants.ROLE_PLAYER;

       if(userdb!=null){

            if(userdb.getProvider()==Provider.SELF){
                logger.info("Updating existing Self-user to also support Google Login ");
                userdb.setProvider(Provider.GOOGLE);
                userdb.setProviderUserId(oauthUser.getName());
                userRepo.save(userdb);
            }

            logger.info("User already exists, proceeding with Google login...");
       }


      // save the user 
      if(userdb == null){
        user.setPassword("GOOGLE_USER_NO_PASSWORD");
        userRepo.save(user);
        logger.info("User Saved : "+email);
      }
      else{
        logger.info("User is already Exist...");
      }

     // Decides the Buttons based on role
     // Button join a Tournament and Become an Organizer
      User sessionUser = (userdb != null) ? userdb : user;
      req.getSession().setAttribute("loggedInUser", sessionUser);

    //   MANUALLY GENERATING REMEMBER-ME ENTRIES FOR THE DATABASE
    // MANUALLY GENERATING REMEMBER-ME ENTRIES FOR THE DATABASE
        try {
            String usernameForToken = (userdb != null) ? userdb.getEmail() : user.getEmail();
            
            PersistentRememberMeToken rememberMeToken = new PersistentRememberMeToken(
                usernameForToken,                    // username
                UUID.randomUUID().toString(),        // series unique tracker
                UUID.randomUUID().toString(),        // random token string values
                new Date()                           // timestamp
            );
            
            // This line physically writes to your `persistent_logins` database table!
            tokenRepository.createNewToken(rememberMeToken);
            logger.info("Successfully dropped remember-me database token record for user: " + usernameForToken);

            // 💡 CHANGE 3 (UPDATED): Encode the cookie into a secure Base64 format matching Spring's protocol
            String[] cookieTokens = new String[] { rememberMeToken.getSeries(), rememberMeToken.getTokenValue() };
            String rawCookieContent = String.join(":", cookieTokens);
            
            // Base64 encode the string "series:tokenValue" securely
            String encodedCookieValue = java.util.Base64.getEncoder().encodeToString(rawCookieContent.getBytes());
            
            Cookie rememberMeCookie = new Cookie("remember-me", encodedCookieValue);
            rememberMeCookie.setMaxAge(86400 * 7);  // Valid for 7 full days
            rememberMeCookie.setPath("/");          // Valid across your entire domain root path space
            rememberMeCookie.setHttpOnly(true);     // Protects against XSS scripting token theft
            res.addCookie(rememberMeCookie);
            logger.info("Remember-Me Base64 encoded browser cookie set successfully.");

        } catch (Exception e) {
            logger.error("Failed to establish auto-login persistence mappings: ", e);
        }

        // CHANGE 4: DYNAMIC ROLE-BASED DASHBOARD ROUTING
        String finalRole = (userdb != null) ? userdb.getRole() : assignedRole1;
        
        if (finalRole.equals(AppConstants.ROLE_ORGANIZER)) {
            logger.info("Routing user to Organizer Dashboard.");
        } else {
            logger.info("Routing user to Public Player Home viewport.");
        }

        new DefaultRedirectStrategy().sendRedirect(req, res, "/home");
    }
}
