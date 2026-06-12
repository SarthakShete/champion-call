package com.champions_Call.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
}
