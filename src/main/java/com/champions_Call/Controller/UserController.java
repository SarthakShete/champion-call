package com.champions_Call.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.champions_Call.Form.signUpForm;
import com.champions_Call.Helper.Message;
import com.champions_Call.model.User;

import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class UserController {

    @GetMapping("/home")
    public String home(){
        return "home";
    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }

    @GetMapping("/signUp")
    public String registration(HttpServletRequest  request,Model model) {

        signUpForm signForm = new signUpForm();

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
        @Valid @ModelAttribute("signForm") signUpForm signForm,
        BindingResult result,
        Model model) {

        User user = new User();
        
        user.setFirstName(signForm.getFirstName());
        user.setLastName(signForm.getLastName());


        
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
}
