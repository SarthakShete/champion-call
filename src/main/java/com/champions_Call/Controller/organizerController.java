package com.champions_Call.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.champions_Call.Form.organizer.OrganizerBasicInfoForm;
import com.champions_Call.Form.organizer.OrganizerIdentityDTO;
import com.champions_Call.model.User;
import com.champions_Call.model.organizer.IdentityVerification;
import com.champions_Call.model.organizer.OrganizerProfile;
import com.champions_Call.model.player.PlayerProfile;
import com.champions_Call.repository.OrganizerProfileRepo;
import com.champions_Call.service.FileUploadService;
import com.champions_Call.service.ImageService;
import com.champions_Call.service.OrganizerService;
import com.champions_Call.service.PhoneVerificationService;
import com.champions_Call.service.PlayerService;
import com.champions_Call.service.SecurityUtils;
import com.champions_Call.service.UserServices;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/organizer")
public class organizerController {

    @Autowired
    private OrganizerProfileRepo organizerProfilRepo;

    @Autowired
    private OrganizerService organizerService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserServices userServices;

    @Autowired 
    private PhoneVerificationService phoneVerificationService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private PlayerService playerService;


    @GetMapping("/dashboard")
    public String showDashboard(HttpServletRequest req,Model model,HttpSession session){

        User user = securityUtils.getLoggedInUser();
        
        if (user == null) {
            return "redirect:/login";
        }

        OrganizerProfile organizerProfile = organizerService.getByUser(user);
        
        model.addAttribute("organizerProfile",organizerProfile);
        return "organizer/Dashboard";
    }

    @GetMapping("/basic-info")
    public String completeBasicInfo(Model model){

        OrganizerBasicInfoForm organizerBasicForm = new OrganizerBasicInfoForm();

        model.addAttribute("organizerBasicForm",organizerBasicForm);
        return "organizer/basic-info";
    }


    @PostMapping("/do-profile")
    public String saveBasicInfo(
        @Valid @ModelAttribute("organizerBasicForm") OrganizerBasicInfoForm basicForm,
        BindingResult result,
        HttpSession session, 
        Model model,
        RedirectAttributes redirectAttributes
    ){

        System.out.println("The do-completProfile called...");
        if(result.hasErrors()){
            return "organizer/basic-info";
        }

        OrganizerProfile isOrganizationName = organizerService.getByOrganizationName(basicForm.getOrganizationName());
        
        OrganizerProfile isPhoneNumber = organizerService.getByPhoneNumber(basicForm.getPhoneNumber());

        if(isOrganizationName!=null){
            System.out.println("The Orgnization name is exist");

            redirectAttributes.addFlashAttribute("error",
        "Organization name already exists. Try another one!");
            return "redirect:/api/organizer/basic-info";
        }

        if(isPhoneNumber!=null){
            System.out.println("The Phone Number is exist");
            redirectAttributes.addFlashAttribute("error",
        "Phone number already exists. Try another one!");
            return "redirect:/api/organizer/basic-info";  
        }
        
        // 💡 INSIDE @PostMapping("/do-profile")
        try {
            String rawPhone = basicForm.getPhoneNumber();
            String formattedPhone = rawPhone.replaceAll("[^\\d+]", ""); 

            if (formattedPhone.length() == 10 && !formattedPhone.startsWith("+")) {
                formattedPhone = "+91" + formattedPhone;
            } else if (formattedPhone.startsWith("91") && formattedPhone.length() == 12) {
                formattedPhone = "+" + formattedPhone;
            } else if (!formattedPhone.startsWith("+")) {
                model.addAttribute("error", "Please provide a valid phone number with a country code (e.g., +91).");
                return "organizer/basic-info";
            }

            // Pass the cleaned, E.164-compliant phone number to Twilio
            phoneVerificationService.sendVerificationCode(formattedPhone);
            
            basicForm.setPhoneNumber(formattedPhone);
            session.setAttribute("pendingProfileData", basicForm);
            
            // ⏳ NEW: Record the exact time the OTP was sent
            session.setAttribute("otpCreationTime", System.currentTimeMillis());
            
            return "redirect:/api/organizer/verify-otp";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to send verification SMS. Please check the phone number.");
            return "organizer/basic-info";
        }
    }

        /**
     * Endpoint to render the OTP verification screen form view layer
     */
    @GetMapping("/verify-otp")
    public String showOtpPage() {
        return "organizer/verify-otp"; 
    }

    /**
     * Endpoint to check user token inputs and cleanly persist final entity maps
     */
    @PostMapping("/submit-otp")
    public String processOtp(
        @RequestParam("otpCode") String otpCode, 
        HttpSession session, 
        RedirectAttributes redirectAttributes
    ) {
        OrganizerBasicInfoForm basicForm = (OrganizerBasicInfoForm) session.getAttribute("pendingProfileData");

        Long otpCreationTime = (Long) session.getAttribute("otpCreationTime");
        
        if (basicForm == null || otpCreationTime == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please request a new OTP.");
            return "redirect:/api/organizer/basic-info";
        }

        // ⏳ NEW: Enforce the time limit (e.g., 45 seconds)
        long elapsedMilliseconds = System.currentTimeMillis() - otpCreationTime;
        long timeLimitInMilliseconds = 45 * 1000; // Change '45' to '30' if you prefer 30 seconds

        if (elapsedMilliseconds > timeLimitInMilliseconds) {
            // Clear out the stale session data so they can't try again with this block
            session.removeAttribute("pendingProfileData");
            session.removeAttribute("otpCreationTime");

            redirectAttributes.addFlashAttribute("error", "Verification timed out! You must enter the OTP within 45 seconds. Please try again.");
            return "redirect:/api/organizer/basic-info"; // Redirect them back to start fresh
        }

        // Verify with Twilio
        boolean isVerified = phoneVerificationService.verifyCode(basicForm.getPhoneNumber(), otpCode);

        if (isVerified) {
            // Token checks out! Fetch target data and construct profile entity mappings
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userServices.getUserByEmail(email);

            OrganizerProfile organizerProfile = organizerService.getByUser(user); 
            if (organizerProfile == null) {
                organizerProfile = new OrganizerProfile();
                organizerProfile.setUser(user);
            }
            
            String fileName = UUID.randomUUID().toString();

            if (basicForm.getProfileImage() != null && !basicForm.getProfileImage().isEmpty()) {
                String fileUrl = imageService.uploadImage(basicForm.getProfileImage(), fileName);
                organizerProfile.setProfilePicture(fileUrl);
                organizerProfile.setCloudinaryImagePublicId(fileName);
            } else {
                String initials = generateInitials(basicForm.getOrganizationName());
                String defaultAvatarUrl = "https://ui-avatars.com/api/?"
                        + "name=" + initials 
                        + "&background=151518" 
                        + "&color=ff914d" 
                        + "&size=200" 
                        + "&bold=true";
                
                organizerProfile.setProfilePicture(defaultAvatarUrl);
                organizerProfile.setCloudinaryImagePublicId(null); 
            }

            // Bind metadata payloads
            organizerProfile.setOrganizationName(basicForm.getOrganizationName());
            organizerProfile.setPhoneNumber(basicForm.getPhoneNumber());
            organizerProfile.setCompleteProfile(true);

            organizerService.saveOrganizer(organizerProfile);
            
            // Clean up temporary session data properties
            session.removeAttribute("pendingProfileData");
            session.removeAttribute("otpCreationTime"); // Clean up timestamp

            redirectAttributes.addFlashAttribute("success", "Profile verified and completed successfully!");
            return "redirect:/api/organizer/dashboard";
        } else {
            // Bad code attempt
            redirectAttributes.addFlashAttribute("error", "Invalid or expired verification code. Try again!");
            return "redirect:/api/organizer/verify-otp";
        }
    }
    @PostMapping("/resend-otp")
    @ResponseBody // Returns direct JSON data back to our JavaScript fetch handler
    public ResponseEntity<Map<String, Object>> resendOtp(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        OrganizerBasicInfoForm basicForm = (OrganizerBasicInfoForm) session.getAttribute("pendingProfileData");
        
        if (basicForm == null) {
            response.put("success", false);
            response.put("message", "Session expired. Please reload the profile page.");
            return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(response);
        }

        try {
            // Trigger Twilio to dispatch a fresh SMS
            phoneVerificationService.sendVerificationCode(basicForm.getPhoneNumber());
            
            // ⏳ Reset the backend expiration timer stamp to right now
            session.setAttribute("otpCreationTime", System.currentTimeMillis());
            
            response.put("success", true);
            response.put("message", "A fresh OTP has been sent successfully!");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to trigger SMS dispatch. Try again shortly.");
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body(response);
        }   
    }
    private String generateInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "CC"; // Fallback abbreviation if completely empty
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        
        // Grab the first letter of the first word
        if (parts.length > 0 && !parts[0].isEmpty()) {
            initials.append(parts[0].charAt(0));
        }
        // Grab the first letter of the last word (if there are multiple words)
        if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
            initials.append(parts[parts.length - 1].charAt(0));
        }
        
        return initials.toString().toUpperCase();
    }



    @GetMapping("/Identity-Info")
    public String completeIdentityInfo(){
        return "organizer/Identity-Info";
    }

    @PostMapping(value="/do-Identity",produces = "application/json")
    public ResponseEntity<?> saveIdentityInfo(
        @ModelAttribute OrganizerIdentityDTO organizerIdentityDTO,
        BindingResult result,
        @RequestParam("idDocument") MultipartFile file,
        Authentication auth
    ){

        User organizer = securityUtils.getLoggedInUser();
        
        if (organizer == null) {
            System.out.println("Organizer is not founded while storing Identity Info");
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if(result.hasErrors()){
            System.out.println("Input not good...");
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", result.getFieldError().getDefaultMessage()
            ));
        }

        

        OrganizerProfile organizerProfile = organizerService.getByUser(organizer);

        
        // Find existing profile or create a new one
        if(organizerProfile==null){
            organizerProfile = new OrganizerProfile();
            organizerProfile.setUser(organizer);
            organizerProfile.initializeDefaults();
        }

        var identity = organizerProfile.getIdentity();
        
        if (identity == null) {
            identity = new IdentityVerification();
            organizerProfile.setIdentity(identity);
        }

        
        if (file != null && !file.isEmpty()) {
            String url = fileUploadService.save(file);  
            identity.setDocumentUrl(url);
        }
        
        identity.setIdType(organizerIdentityDTO.getIdType());
        identity.setIdNumber(organizerIdentityDTO.getIdNumber());

        organizerService.saveOrganizer(organizerProfile);   
        
        return ResponseEntity.ok(Map.of(
            "success", true, 
            "message", "Identity info is stored Successfully..."
        ));
    }

    @GetMapping("/player/{playerId}/details")
    public ResponseEntity<?> getPlayerDetailsForOrganizer(@PathVariable Long playerId){
        PlayerProfile profile = playerService.getPlayerProfileById(playerId);
        if (profile == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Player not found"));
        }
        return ResponseEntity.ok(profile);
    }

    
} 
