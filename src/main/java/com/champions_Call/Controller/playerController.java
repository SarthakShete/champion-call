package com.champions_Call.Controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.champions_Call.model.User;
import com.champions_Call.service.UserServices;

@Controller
@RequestMapping("/player")
public class playerController {

  @Autowired
  private UserServices userServices;

  @RequestMapping("/profile")
  public String playerProfile(Principal principle,Model model){

    // here we have to check that if user complete their Profile or not
    // Complete Profile => If user Logged In using GOOGle Provider that time we dont take it phone Number..
    // By Default we store the Phone_Profile false...
    
    // first getting the email of Current user
  //    if (principle == null) {
  //       return "redirect:/login";
  //   }

  //   String email;

  //   // ✅ Handle OAuth2 login (Google)
  //   if (principle instanceof OAuth2AuthenticationToken oauthToken) {
  //       OAuth2User oauthUser = oauthToken.getPrincipal();
  //       email = oauthUser.getAttribute("email"); // ✅ gets real email
  //   } else {
  //       // ✅ Handle normal form login
  //       email = principle.getName();
  //   }

  //   System.out.println("Resolved email: " + email);

  //   User user = userServices.getUserByEmail(email);

  //   if (user == null) {
  //       return "redirect:/login";
  //   }

  //   if (!user.isPhoneVerified()) {
  //       return "redirect:/player/completeProfile";
  //   }

  //   model.addAttribute("user", user);
  //   return "player/profile";
  // }

  // @RestMapping("/completeProfile")
  // public String completeProfile(){
  //   return "player/completeProfile"
  // }

  return "home";
}
} 
