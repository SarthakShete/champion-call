package com.champions_Call.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.champions_Call.model.organizer.OrganizerProfile;
import com.champions_Call.model.player.PlayerProfile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name="users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private OrganizerProfile organizerProfile;

    @OneToOne(mappedBy="user", cascade=CascadeType.ALL)
    private PlayerProfile playerProfile;

    @Column(name="user_first", nullable=false)
    private String firstName;

    @Column(name="user_last", nullable=false)
    private String lastName;

    @Column(unique=true, nullable=false)
    private String email;

    private String password;

    @Column(unique=true, length=255)
    private String phoneNumber;

    private String role;

    @Column(name = "profile_pic", length = 1000)
    private String profilePic;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Provider provider = Provider.SELF;

    private String providerUserId;

    

    @Builder.Default
    private boolean emailVerified = false;

    
    // ===== Getters =====

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public Provider getProvider() {
        return provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }
    public boolean getEmailVerified() {
        return emailVerified;
    }

    public String getProfilePic() {
        return profilePic;
    }
    public OrganizerProfile getOrganizerProfile() {
        return organizerProfile;
    }

    // ===== Setters =====

    public void setId(int id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
    public void setOrganizerProfile(OrganizerProfile organizerProfile) {
        this.organizerProfile = organizerProfile;
    }


    // @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private OrganizerProfile organizerProfile;

    // @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private PlayerProfile playerProfile;

    // ===== UserDetails Methods =====

    @Override   
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + this.role.toUpperCase());

        return List.of(authority);
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; 
    }

    public PlayerProfile getPlayerProfile() {
        return playerProfile;
    }

    public void setPlayerProfile(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }
}