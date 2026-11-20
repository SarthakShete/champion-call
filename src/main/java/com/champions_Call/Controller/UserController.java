package com.champions_Call.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.champions_Call.Form.SignUpForm;
import com.champions_Call.Form.Test;
import com.champions_Call.Helper.Message;
import com.champions_Call.Helper.MessageType;
import com.champions_Call.model.User;
import com.champions_Call.service.UserServices;

import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class UserController {

    @Autowired
    private UserServices userServices;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/home")
    public String home(Model model, Authentication authentication, HttpSession session, HttpServletResponse response){

        // Prevent any stale caching regardless of role (belt-and-suspenders)
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        User currentUser = null;

        boolean isLoggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        if (isLoggedIn) {
            // Prefer session (already set by your success handler) to avoid extra DB hit
            Object sessionUser = session.getAttribute("loggedInUser");

            if (sessionUser instanceof User) {
                currentUser = (User) sessionUser;
            } else {
                // Fallback: fetch fresh from DB using the authenticated email
                String email = authentication.getName(); // adjust if your principal's "name" isn't email
                currentUser = userServices.getUserByEmail(email);
            }
        }

        model.addAttribute("currentUser", currentUser);

        return "home";
    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }

    @GetMapping("/signUp")
    public String registration(HttpServletRequest  request,Model model) {

        SignUpForm signForm = new SignUpForm();

        signForm.setRole("PLAYER");

        Cookie[] cookies = request.getCookies();

        if(cookies == null){
            System.out.println("Cookie is Null");
        }
        else{
            for (Cookie cookie : cookies) {
                if ("Role".equals(cookie.getName())) {
                    System.out.println("Cookie is working");
                    signForm.setRole(cookie.getValue());
                    System.out.println("The Role is after: "+cookie.getValue()); 
                    break;
                }
            }
        }

        model.addAttribute("signForm",signForm);

        System.out.println("The Role is : "+signForm.getRole());
        
        return "signUp";    
    }

    @PostMapping("/do-signUp")
    public String Registration(
        @Valid @ModelAttribute("signForm") SignUpForm signForm,
        BindingResult result,
        Model model) {

        if(result.hasErrors()){
            return "signUp";
        }

        User user = new User();
        
        user.setFirstName(signForm.getFirstName());
        user.setLastName(signForm.getLastName());
        user.setEmail(signForm.getEmail());
        user.setPassword(signForm.getPassword());
        user.setPhoneNumber(signForm.getPhoneNumber());
        user.setRole(signForm.getRole());

        User doesEmailExist = userServices.getUserByEmail(signForm.getEmail());
        User doesPhoneNumberExist = userServices.getUserByPhoneNumber(signForm.getPhoneNumber());

        if(doesEmailExist!=null){
            System.out.println("The Email is exist");
            Message errorMessage = new Message();            
            errorMessage.setContent("This email is already registered! Please use a different one.");
            errorMessage.setType(MessageType.red);
                               

            model.addAttribute("message", errorMessage);
            return "signUp";
        }
        else if(doesPhoneNumberExist!=null){
            System.out.println("The phone Number is Exist");
            Message phoneNumberError = new Message();
            phoneNumberError.setContent("This phone number is already registered! Please use another one.");
            phoneNumberError.setType(MessageType.red); 

            model.addAttribute("message", phoneNumberError);
            return "signUp";
        }
        
        
        // This only runs if BOTH email and phone number are completely unique!
        User signUser = userServices.saveUser(user);
        System.out.println("User Saved Successfullyyy...");

        Message savedMsg = new  Message();
        savedMsg.setContent("Registration Successful! Please log in to continue.");
        savedMsg.setType(MessageType.green);
                        
        System.out.println(user);
       model.addAttribute("message", savedMsg);
       return "signUp";
        
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
}
