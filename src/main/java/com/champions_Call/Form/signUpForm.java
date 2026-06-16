package com.champions_Call.Form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class signUpForm{

  @NotBlank(message="First Name is Required...")
  private String firstName;

  @NotBlank(message="Second Name is Required...")
  private String lastName;

  @Email(message="Email is Required...")
  @NotBlank(message ="Email is Required...")
  private String email;

  @NotBlank(message="Role is Required")
  private String role;

  
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
      message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and be at least 8 characters long"
  )
  private String password;

  @NotBlank(message="Phone Number is Required...")
  @Size(min=10,message="Invalid Phone Number")
  private String phoneNumber;

  public void setRole(String Role){
    this.role = Role;
  }

  public String getRole(){
    return this.role;
  }
}
