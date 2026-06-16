package com.champions_Call.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Data
public class User implements UserDetails{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="user_first",nullable=false)
    private String firstName;

    @Column(name="user_last",nullable=false)
    private String lastName;

    @Column(unique=true,nullable=false)
    private String email;


    private String password;

    @Column(unique=true,length=255)
    private String phoneNumber;

    
    private String role;


    @Builder.Default
    @Enumerated(value=EnumType.STRING)
    private Provider provider = Provider.SELF;

    private String providerUserId;

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private boolean emailVerified = false;

    @Builder.Default
    private boolean phoneVerified = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Dynamically add the "ROLE_" prefix so that "PLAYER" becomes "ROLE_PLAYER"
        // This makes Spring Security's .hasRole() methods work perfectly!
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + this.role);
        
        return List.of(authority);
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    
    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return this.enabled;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

}
